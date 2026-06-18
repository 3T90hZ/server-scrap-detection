package com.scrapDetection.mapper;

import com.scrapDetection.dto.transaction.TransactionRequestDTO;
import com.scrapDetection.dto.transaction.TransactionResponseDTO;
import com.scrapDetection.dto.transaction.TransactionSummaryDTO;
import com.scrapDetection.entity.Transaction;
import com.scrapDetection.entity.TransactionTotal;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TransactionMapper {

    public Transaction toEntity(TransactionRequestDTO dto) {
        if (dto == null) return null;

        Transaction transaction = new Transaction();
        transaction.setWeight(dto.getWeight());
        // material and customer will be set in Service layer
        return transaction;
    }

    public TransactionResponseDTO toResponseDTO(Transaction entity) {
        if (entity == null) return null;

        TransactionTotal total = entity.getTransactionTotal();

        return TransactionResponseDTO.builder()
                .transactionId(entity.getTransactionId())
                .materialId(entity.getMaterial().getMaterialId())
                .itemName(entity.getMaterial().getItemName())
                .weight(entity.getWeight())
                .totalWorth(total != null ? total.getTotalWorth() : null)
                .customerId(entity.getCustomer().getAccountId())
                .customerName(entity.getCustomer().getAccountName())
                .ownerOrStaffId(entity.getOwnerOrStaff() != null ? entity.getOwnerOrStaff().getAccountId() : null)
                .staffName(entity.getOwnerOrStaff() != null ? entity.getOwnerOrStaff().getAccountName() : null)
                .yardId(entity.getMaterial().getScrapYard().getYardId())
                .yardName(entity.getMaterial().getScrapYard().getYardName())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public TransactionSummaryDTO toSummaryDTO(Transaction entity) {
        if (entity == null) return null;

        return TransactionSummaryDTO.builder()
                .transactionId(entity.getTransactionId())
                .itemName(entity.getMaterial().getItemName())
                .weight(entity.getWeight())
                .totalWorth(entity.getTransactionTotal() != null ? entity.getTransactionTotal().getTotalWorth() : null)
                .customerName(entity.getCustomer().getAccountName())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public List<TransactionResponseDTO> toResponseDTOList(List<Transaction> transactions) {
        return transactions.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<TransactionSummaryDTO> toSummaryDTOList(List<Transaction> transactions) {
        return transactions.stream()
                .map(this::toSummaryDTO)
                .collect(Collectors.toList());
    }
}