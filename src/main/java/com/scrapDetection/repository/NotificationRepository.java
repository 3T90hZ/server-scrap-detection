package com.scrapDetection.repository;

import com.scrapDetection.entity.Notification;
import com.scrapDetection.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByRecipientAccountIdOrderByCreatedAtDesc(
            Long recipientId,
            Pageable pageable
    );
    Optional<Notification> findByRecipientAccountIdAndSenderAccountIdAndTypeAndIsAcceptedIsNull(
            Long recipientId,
            Long senderId,
            NotificationType type
    );

    long countByRecipientAccountIdAndIsReadFalse(Long recipientId);
    List<Notification> findByRecipientAccountIdAndIsReadFalse(Long recipientId);

    @Query("""
        SELECT n
        FROM Notification n
        WHERE n.recipient.accountId = :recipientId
          AND n.type = com.scrapDetection.entity.NotificationType.STAFF_INVITATION
          AND n.isAccepted IS NULL
          AND n.expiredAt > CURRENT_TIMESTAMP
        ORDER BY n.createdAt DESC
    """)
    List<Notification> findPendingInvitationsByRecipientId(
            @Param("recipientId") Long recipientId
    );
}
