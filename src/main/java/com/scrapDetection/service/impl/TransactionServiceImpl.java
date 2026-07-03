package com.scrapDetection.service.impl;

import com.scrapDetection.dto.transaction.TransactionRequestDTO;
import com.scrapDetection.dto.transaction.TransactionResponseDTO;
import com.scrapDetection.dto.transaction.TransactionSummaryDTO;
import com.scrapDetection.entity.Account;
import com.scrapDetection.entity.Material;
import com.scrapDetection.entity.Transaction;
import com.scrapDetection.entity.TransactionTotal;
import com.scrapDetection.exception.InvalidRequestException;
import com.scrapDetection.exception.ResourceNotFoundException;
import com.scrapDetection.mapper.TransactionMapper;
import com.scrapDetection.repository.MaterialRepository;
import com.scrapDetection.repository.TransactionRepository;
import com.scrapDetection.service.AccountService;
import com.scrapDetection.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final MaterialRepository materialRepository;
    private final AccountService accountService;
    private final TransactionMapper transactionMapper;

    @Override
    public TransactionResponseDTO createTransaction(TransactionRequestDTO requestDTO) {
        // Get current logged-in user (Staff or Yard Owner)
        Account currentUser = accountService.getCurrentUser();

        // Get material
        Material material = materialRepository.findById(requestDTO.getMaterialId())
                .orElseThrow(() -> new ResourceNotFoundException("Material", requestDTO.getMaterialId()));

        // Verify material belongs to the same yard as the staff/owner
        if (!material.getScrapYard().getYardId().equals(currentUser.getScrapYard().getYardId())) {
            throw new InvalidRequestException("You can only create transactions for materials in your yard");
        }

        // Create transaction
        Transaction transaction = transactionMapper.toEntity(requestDTO);
        transaction.setMaterial(material);
        transaction.setCustomer(currentUser);           // Customer is the one making the transaction
        transaction.setCreatedBy(currentUser);       // Staff/Owner processing the transaction

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Create TransactionTotal
        TransactionTotal total = new TransactionTotal();
        total.setTransaction(savedTransaction);
        total.setTotalWorth(requestDTO.getWeight() * material.getItemPrice());
        savedTransaction.setTransactionTotal(total);

        // Save again to persist total
        savedTransaction = transactionRepository.save(savedTransaction);

        return transactionMapper.toResponseDTO(savedTransaction);
    }

    @Override
    public TransactionResponseDTO getTransactionById(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", transactionId));
        return transactionMapper.toResponseDTO(transaction);
    }

    @Override
    public List<TransactionSummaryDTO> getTransactionsByCustomer(Long customerId) {
        List<Transaction> transactions = transactionRepository.findByCustomerAccountId(customerId);
        return transactionMapper.toSummaryDTOList(transactions);
    }

    @Override
    public List<TransactionSummaryDTO> getTransactionsByYard(Long yardId) {
        List<Transaction> transactions = transactionRepository.findByMaterialScrapYardYardId(yardId);
        return transactionMapper.toSummaryDTOList(transactions);
    }

    @Override
    public List<TransactionSummaryDTO> getTransactionsByStaff(Long staffId) {
        List<Transaction> transactions = transactionRepository.findByCreated_byAccountId(staffId);
        return transactionMapper.toSummaryDTOList(transactions);
    }

    @Override
    public List<TransactionSummaryDTO> getTransactionSummaries() {
        List<Transaction> transactions = transactionRepository.findAll();
        return transactionMapper.toSummaryDTOList(transactions);
    }

    @Override
    public List<TransactionResponseDTO> getTransactionsByDateRange(LocalDateTime start, LocalDateTime end) {
        List<Transaction> transactions = transactionRepository.findByCreatedAtBetween(start, end);
        return transactionMapper.toResponseDTOList(transactions);
    }


}