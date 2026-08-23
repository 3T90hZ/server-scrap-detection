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

    @NotNull(message = "Accept invite value is required")
    private Boolean acceptInvite;
}