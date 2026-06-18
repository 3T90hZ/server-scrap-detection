package com.scrapDetection.service;

import com.scrapDetection.dto.transaction.TransactionRequestDTO;
import com.scrapDetection.dto.transaction.TransactionResponseDTO;
import com.scrapDetection.dto.transaction.TransactionSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {

    // Create a new transaction (Staff or Yard Owner)
    TransactionResponseDTO createTransaction(TransactionRequestDTO requestDTO);

    // Get transaction by ID
    TransactionResponseDTO getTransactionById(Long transactionId);

    // Get all transactions for a customer
    List<TransactionResponseDTO> getTransactionsByCustomer(Long customerId);

    // Get all transactions in a yard
    List<TransactionResponseDTO> getTransactionsByYard(Long yardId);

    // Get transactions handled by a specific staff
    List<TransactionResponseDTO> getTransactionsByStaff(Long staffId);

    // Get transactions with pagination
    Page<TransactionResponseDTO> getAllTransactions(Pageable pageable);

    // Get transaction summary (lighter version)
    List<TransactionSummaryDTO> getTransactionSummaries();

    // Get transactions by date range
    List<TransactionResponseDTO> getTransactionsByDateRange(LocalDateTime start, LocalDateTime end);
}