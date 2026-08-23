package com.scrapDetection.service.impl;

import com.scrapDetection.dto.notification.AcceptInviteRequestDTO;
import com.scrapDetection.dto.notification.NotificationResponseDTO;
import com.scrapDetection.entity.*;
import com.scrapDetection.exception.InvalidRequestException;
import com.scrapDetection.exception.ResourceNotFoundException;
import com.scrapDetection.mapper.NotificationMapper;
import com.scrapDetection.repository.AccountRepository;
import com.scrapDetection.repository.NotificationRepository;
import com.scrapDetection.service.AccountService;
import com.scrapDetection.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final AccountService accountService;
    private final NotificationMapper notificationMapper;
    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponseDTO> getMyNotifications(Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByRecipientAccountIdOrderByCreatedAtDesc(accountService.getCurrentUser().getAccountId(), pageable);
        return notifications.map(notificationMapper::toNotificationResponseDTO);
    }

    @Override
    @Transactional
    public void acceptNotification(Long notificationId,AcceptInviteRequestDTO dto) {

        Account recipient = accountService.getCurrentUser();

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Notification " + notificationId
                        ));

        // Make sure this notification belongs to the current user
        if (!notification.getRecipient().getAccountId()
                .equals(recipient.getAccountId())) {

            throw new InvalidRequestException(
                    "No permission on this notification"
            );
        }

        // Make sure it is an invitation
        if (notification.getType() != NotificationType.STAFF_INVITATION) {
            throw new InvalidRequestException(
                    "This notification is not a staff invitation"
            );
        }

        // Already answered
        if (notification.getIsAccepted() != null) {
            throw new InvalidRequestException(
                    "This invitation has already been answered"
            );
        }

        // Check expiration
        if (notification.getExpiredAt() == null ||
                !notification.getExpiredAt().isAfter(LocalDateTime.now())) {

            throw new InvalidRequestException(
                    "Notification has expired"
            );
        }

        Account sender = notification.getSender();

        ScrapYard scrapYard = getValidSenderScrapYard(sender);

        // User can only accept if they are currently a customer
        if (recipient.getRole() != Role.CUSTOMER ||
                recipient.getScrapYard() != null) {

            throw new InvalidRequestException(
                    "You already belong to a yard"
            );
        }

        // REJECT
        if (!dto.getAcceptInvite()) {
            notification.setIsAccepted(false);
            notification.setIsRead(true);
            return;
        }

        // ACCEPT
        notification.setIsAccepted(true);
        notification.setIsRead(true);

        recipient.setScrapYard(scrapYard);
        recipient.setRole(Role.STAFF);

        // Reject other pending invitations
        List<Notification> pending =
                notificationRepository.findPendingInvitationsByRecipientId(
                        recipient.getAccountId()
                );

        pending.forEach(n -> {
            if (!n.getNotificationId().equals(notification.getNotificationId())) {
                n.setIsAccepted(false);
                n.setIsRead(true);
            }
        });
    }

    @Transactional
    @Override
    public void createBillNotification(Account recipient, Bill bill) {
        Notification billNotification = Notification.builder()
                .title("Thông báo giao dịch thành công")
                .message(
                        "Giao dịch của bạn đã được tạo thành công. Tổng tiền: "
                                + bill.getTotalWorth()
                                + " VND."
                )
                .type(NotificationType.BILL_CREATED)
                .createdAt(LocalDateTime.now())
                .sender(bill.getCreatedBy())
                .recipient(recipient)
                .bill(bill)
                .build();

        notificationRepository.save(billNotification);
    }

    @Transactional
    public void createInviteNotification(Long recipientId, Long senderId) {

        Account recipient = accountRepository.findById(recipientId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account " + recipientId));

        Account sender = accountRepository.findById(senderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account " + senderId));

        ScrapYard scrapYard = getValidSenderScrapYard(sender);

        if (recipient.getRole() != Role.CUSTOMER ||
                recipient.getScrapYard() != null) {

            throw new InvalidRequestException(
                    "This account cannot become staff"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        Optional<Notification> existing =
                notificationRepository
                        .findByRecipientAccountIdAndSenderAccountIdAndTypeAndIsAcceptedIsNull(
                                recipientId,
                                senderId,
                                NotificationType.STAFF_INVITATION
                        );

        if (existing.isPresent()) {

            Notification invitation = existing.get();

            if (invitation.getExpiredAt() != null &&
                    invitation.getExpiredAt().isAfter(now)) {

                throw new InvalidRequestException(
                        "Already invited this account"
                );
            }
        }

        Notification inviteNotification = Notification.builder()
                .title("Thư mời nhân viên")
                .message(
                        "Bạn được mời trở thành nhân viên của vựa: "
                                + scrapYard.getYardName()
                                + ". Thư mời sẽ hết hạn sau 12 giờ."
                )
                .type(NotificationType.STAFF_INVITATION)
                .createdAt(now)
                .sender(sender)
                .recipient(recipient)
                .expiredAt(now.plusHours(12))
                .build();

        notificationRepository.save(inviteNotification);
    }

    private ScrapYard getValidSenderScrapYard(Account sender) {
        if (sender == null ||
                sender.getRole() != Role.YARD_OWNER ||
                "INACTIVE".equals(sender.getStatus())) {

            throw new InvalidRequestException("Yard owner not found");
        }

        ScrapYard scrapYard = sender.getScrapYard();

        if (scrapYard == null ||
                "INACTIVE".equals(scrapYard.getStatus())) {

            throw new InvalidRequestException("Scrap Yard is invalid");
        }

        return scrapYard;
    }
}
