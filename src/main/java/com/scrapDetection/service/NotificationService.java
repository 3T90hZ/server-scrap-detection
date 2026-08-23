package com.scrapDetection.service;


import com.scrapDetection.dto.notification.AcceptInviteRequestDTO;
import com.scrapDetection.dto.notification.NotificationResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationService {
    Page<NotificationResponseDTO> getMyNotifications(Pageable pageable);

    @Transactional
    void acceptNotification(AcceptInviteRequestDTO dto);

    @Transactional
    void createBillNotification(Long recipientId, Long billId);

    @Transactional
    void createInviteNotification(Long recipientId, Long senderId);
}
