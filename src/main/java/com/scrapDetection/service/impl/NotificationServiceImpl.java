package com.scrapDetection.service.impl;

import com.scrapDetection.dto.notification.AcceptInviteRequestDTO;
import com.scrapDetection.dto.notification.NotificationResponseDTO;
import com.scrapDetection.entity.*;
import com.scrapDetection.exception.InvalidRequestException;
import com.scrapDetection.exception.ResourceNotFoundException;
import com.scrapDetection.mapper.NotificationMapper;
import com.scrapDetection.repository.AccountRepository;
import com.scrapDetection.repository.NotificationRepository;
import com.scrapDetection.service.CurrentUserService;
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
    private final NotificationMapper notificationMapper;
    private final AccountRepository accountRepository;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponseDTO> getMyNotifications(Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByRecipientAccountIdOrderByCreatedAtDesc(currentUserService.getCurrentUser().getAccountId(), pageable);
        return notifications.map(notificationMapper::toNotificationResponseDTO);
    }

    @Override
    @Transactional
    public void acceptNotification(Long notificationId,AcceptInviteRequestDTO dto) {

        Account recipient = currentUserService.getCurrentUser();

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Thông báo " + notificationId
                        ));

        // Make sure this notification belongs to the current user
        if (!notification.getRecipient().getAccountId()
                .equals(recipient.getAccountId())) {

            throw new InvalidRequestException(
                    "Không có quyền"
            );
        }

        // Make sure it is an invitation
        if (notification.getType() != NotificationType.STAFF_INVITATION) {
            throw new InvalidRequestException(
                    "Không phải lời mơi làm nhân viên!"
            );
        }

        // Already answered
        if (notification.getIsAccepted() != null) {
            throw new InvalidRequestException(
                    "Lời mời này đã được phản hồi"
            );
        }

        // Check expiration
        if (notification.getExpiredAt() == null ||
                !notification.getExpiredAt().isAfter(LocalDateTime.now())) {

            throw new InvalidRequestException(
                    "Lời mời đã hết hạn"
            );
        }

        Account sender = notification.getSender();

        ScrapYard scrapYard = getValidSenderScrapYard(sender);

        // User can only accept if they are currently a customer
        if (recipient.getRole() != Role.CUSTOMER ||
                recipient.getScrapYard() != null) {

            throw new InvalidRequestException(
                    "Bạn đã thuộc vè một vựa"
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
                        new ResourceNotFoundException("Tài khoản " + recipientId));

        Account sender = accountRepository.findById(senderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Tài khoản " + senderId));

        ScrapYard scrapYard = getValidSenderScrapYard(sender);

        if (recipient.getRole() != Role.CUSTOMER ||
                recipient.getScrapYard() != null) {

            throw new InvalidRequestException(
                    "Tài khoản này không thể trở thành nhân viên"
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
                        "Tài khoản này đã được mời!"
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
                AccountStatus.INACTIVE.equals(sender.getStatus())) {

            throw new InvalidRequestException("Không tìm thấy chủ vựa");
        }

        ScrapYard scrapYard = sender.getScrapYard();

        if (scrapYard == null ||
            YardStatus.INACTIVE.equals(scrapYard.getStatus())) {

            throw new InvalidRequestException("Vựa không hợp lệ!");
        }

        return scrapYard;
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        return notificationRepository.countByRecipientAccountIdAndIsReadFalse(
                currentUserService.getCurrentUser().getAccountId()
        );
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Account currentUser = currentUserService.getCurrentUser();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Thông báo " + notificationId));

        if (!notification.getRecipient().getAccountId().equals(currentUser.getAccountId())) {
            throw new InvalidRequestException("Không có quyền xem thông báo này");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        Account currentUser = currentUserService.getCurrentUser();
        List<Notification> unreadList = notificationRepository.findByRecipientAccountIdAndIsReadFalse(currentUser.getAccountId());

        unreadList.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unreadList);
    }
}
