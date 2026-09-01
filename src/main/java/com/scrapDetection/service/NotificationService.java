package com.scrapDetection.service;


import com.scrapDetection.dto.notification.AcceptInviteRequestDTO;
import com.scrapDetection.dto.notification.NotificationResponseDTO;
import com.scrapDetection.entity.Account;
import com.scrapDetection.entity.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationService {
    Page<NotificationResponseDTO> getMyNotifications(Pageable pageable);

    @Transactional
    void acceptNotification(Long notificationId, AcceptInviteRequestDTO dto);

    @Transactional
    void createBillNotification(Account recipient, Bill bill);

    @Transactional
    void createInviteNotification(Long recipientId, Long senderId);

    long getUnreadCount();

    @Transactional
    void markAsRead(Long notificationId);

    @Transactional
    void markAllAsRead();
}
