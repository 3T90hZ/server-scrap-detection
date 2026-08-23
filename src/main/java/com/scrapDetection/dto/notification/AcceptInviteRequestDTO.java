package com.scrapDetection.dto.notification;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcceptInviteRequestDTO {
    @NotNull(message = "Notification id is required")
    Long notificationId;

    @NotNull
    Boolean acceptInvite;
}
