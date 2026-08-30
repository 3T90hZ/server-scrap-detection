package com.scrapDetection.mapper;

import com.scrapDetection.dto.notification.NotificationResponseDTO;
import com.scrapDetection.entity.Notification;
import com.scrapDetection.entity.NotificationType;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    public NotificationResponseDTO toNotificationResponseDTO(Notification entity) {
        if (entity == null) return null;
        return NotificationResponseDTO.builder()
                .notificationId(entity.getNotificationId())
                .title(entity.getTitle())
                .type(entity.getType())
                .message(entity.getMessage())
                .isRead(entity.getIsRead())
                .isAccepted(entity.getIsAccepted())
                .createdAt(entity.getCreatedAt())
                .expiredAt(entity.getExpiredAt())
                .billId(entity.getBill() != null ? entity.getBill().getBillId() : null)
                .senderName(entity.getSender() != null ? entity.getSender().getAccountName() : null)
                .senderPhone(entity.getSender() != null ? entity.getSender().getPhoneNumbers() : null)
                .build();
    }
}
