package com.scrapDetection.controller;

import com.scrapDetection.dto.transaction.TransactionRequestDTO;
import com.scrapDetection.dto.transaction.TransactionResponseDTO;
import com.scrapDetection.dto.transaction.TransactionSummaryDTO;
import com.scrapDetection.service.AccountService;
import com.scrapDetection.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;
    private final AccountService accountService;

    // Create transaction
    @PreAuthorize("hasAnyRole('STAFF', 'YARD_OWNER')")
    @PostMapping
    public ResponseEntity<TransactionResponseDTO> createTransaction(
            @Valid @RequestBody TransactionRequestDTO requestDTO) {

        TransactionResponseDTO response = transactionService.createTransaction(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Get transactions in owner's yard
    @PreAuthorize("hasRole('YARD_OWNER')")
    @GetMapping("/my-yard")
    public ResponseEntity<List<TransactionSummaryDTO>> getMyYardTransactions() {
        Long yardId = getCurrentUserYardId();
        List<TransactionSummaryDTO> response = transactionService.getTransactionsByYard(yardId);
        return ResponseEntity.ok(response);
    }

    // Get staff's transactions
    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/my-transactions")
    public ResponseEntity<List<TransactionSummaryDTO>> getMyTransactions() {
        Long currentUserId = getCurrentUserId();
        List<TransactionSummaryDTO> response = transactionService.getTransactionsByStaff(currentUserId);
        return ResponseEntity.ok(response);
    }

    // Get all transaction by customer
    @PreAuthorize("hasAnyRole('STAFF', 'YARD_OWNER','CUSTOMER')")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<TransactionSummaryDTO>> getTransactionsByCustomer(@PathVariable Long customerId) {
        List<TransactionSummaryDTO> response = transactionService.getTransactionsByCustomer(customerId);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/summary")
    public ResponseEntity<List<TransactionSummaryDTO>> getTransactionSummaries() {
        List<TransactionSummaryDTO> response = transactionService.getTransactionSummaries();
        return ResponseEntity.ok(response);
    }

    // Get transactions by date range
    @GetMapping("/date-range")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByDateRange(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {

        List<TransactionResponseDTO> response = transactionService.getTransactionsByDateRange(start, end);
        return ResponseEntity.ok(response);
    }
    // ==================== Helper Methods ====================

    private Long getCurrentUserId() {
        var currentUser = accountService.getCurrentUser();
        return currentUser.getAccountId();
    }

    private Long getCurrentUserYardId() {
        var currentUser = accountService.getCurrentUser();

        if (currentUser.getScrapYard() == null) {
            throw new com.scrapDetection.exception.InvalidRequestException("You are not assigned to any scrap yard");
        }

        return currentUser.getScrapYard().getYardId();
    }
}
