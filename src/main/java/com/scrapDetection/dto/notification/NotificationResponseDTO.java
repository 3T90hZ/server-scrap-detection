package com.scrapDetection.dto.notification;

import com.scrapDetection.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDTO {
    private Long notificationId;

    private String title;

    private String message;

    private NotificationType type;

    private Boolean isRead;

    private Boolean isAccepted;

    private LocalDateTime createdAt;

    private LocalDateTime expiredAt;

    private Long billId;
}
