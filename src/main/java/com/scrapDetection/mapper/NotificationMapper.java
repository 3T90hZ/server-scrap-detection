package com.scrapDetection.mapper;

import com.scrapDetection.dto.notification.NotificationResponseDTO;
import com.scrapDetection.entity.Notification;
import com.scrapDetection.entity.NotificationType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NotificationMapper {
    public NotificationResponseDTO toNotificationResponseDTO(Notification entity) {
        if (entity == null) return null;
        return NotificationResponseDTO.builder()
                .notificationId(entity.getNotificationId())
                .title(entity.getTitle())
                .type(NotificationType.BILL_CREATED)
                .message(entity.getMessage())
                .isRead(entity.getIsRead())
                .createdAt(entity.getCreatedAt())
                .billId(entity.getBill().getBillId())
                .build();
    }

    public List<NotificationResponseDTO> toNotificationResponseList(List<Notification> entityList) {
        return entityList.stream()
                .map(this::toNotificationResponseDTO)
                .collect(Collectors.toList());
    }
}
