package com.scrapDetection.mapper;

import com.scrapDetection.dto.bill.*;
import com.scrapDetection.entity.Bill;
import com.scrapDetection.entity.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BillMapper {

    public BillItemResponseDTO toItemResponseDTO(Transaction tx) {
        if (tx == null) return null;

        return BillItemResponseDTO.builder()
                .transactionId(tx.getTransactionId())
                .materialId(tx.getMaterial().getMaterialId())
                .itemName(tx.getMaterial().getItemName())
                .weight(tx.getWeight())
                .lineWorth(tx.getLineWorth())
                .isOverridden(tx.getIsOverridden())
                .originalMaterialId(tx.getOriginalMaterial() != null ? tx.getOriginalMaterial().getMaterialId() : null)
                .originalMaterialName(tx.getOriginalMaterial() != null ? tx.getOriginalMaterial().getItemName() : null)
                .originalWeight(tx.getOriginalWeight())
                .build();
    }

    public BillResponseDTO toResponseDTO(Bill bill) {
        if (bill == null) return null;

        return BillResponseDTO.builder()
                .billId(bill.getBillId())
                .customerId(bill.getCustomer().getAccountId())
                .customerName(bill.getCustomer().getAccountName())
                .createdById(bill.getCreatedBy() != null ? bill.getCreatedBy().getAccountId() : null)
                .createdByName(bill.getCreatedBy() != null ? bill.getCreatedBy().getAccountName() : null)
                .createdAt(bill.getCreatedAt())
                .totalWorth(bill.getTotalWorth())
                .yardId(bill.getTransactions().isEmpty() ? null :
                        bill.getTransactions().getFirst().getMaterial().getScrapYard().getYardId())
                .yardName(bill.getTransactions().isEmpty() ? null :
                        bill.getTransactions().getFirst().getMaterial().getScrapYard().getYardName())
                .items(bill.getTransactions().stream()
                        .map(this::toItemResponseDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    public BillSummaryDTO toSummaryDTO(Bill bill) {
        if (bill == null) return null;

        return BillSummaryDTO.builder()
                .billId(bill.getBillId())
                .customerName(bill.getCustomer().getAccountName())
                .createdByName(bill.getCreatedBy() != null ? bill.getCreatedBy().getAccountName() : null)
                .totalWorth(bill.getTotalWorth())
                .itemCount(bill.getTransactions().size())
                .createdAt(bill.getCreatedAt())
                .hasOverridden(bill.getTransactions().stream().anyMatch(t -> Boolean.TRUE.equals(t.getIsOverridden())))
                .build();
    }

    public List<BillResponseDTO> toResponseDTOList(List<Bill> bills) {
        return bills.stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    public List<BillSummaryDTO> toSummaryDTOList(List<Bill> bills) {
        return bills.stream().map(this::toSummaryDTO).collect(Collectors.toList());
    }
}