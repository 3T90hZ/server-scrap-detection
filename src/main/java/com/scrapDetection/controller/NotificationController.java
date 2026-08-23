package com.scrapDetection.controller;

import com.scrapDetection.dto.notification.AcceptInviteRequestDTO;
import com.scrapDetection.dto.notification.NotificationResponseDTO;
import com.scrapDetection.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'YARD_OWNER', 'STAFF', 'CUSTOMER')")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<NotificationResponseDTO>> getMyNotifications(
            @PageableDefault(size = 10, sort = "createdAt")
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                notificationService.getMyNotifications(pageable)
        );
    }

    @PatchMapping("/{notificationId}/invitation")
    public ResponseEntity<Void> respondToInvitation(
            @PathVariable Long notificationId,
            @Valid @RequestBody AcceptInviteRequestDTO dto
    ) {

        notificationService.acceptNotification(notificationId, dto);

        return ResponseEntity.noContent().build();
    }
}