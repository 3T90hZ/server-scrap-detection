package com.scrapDetection.service.impl;

import com.scrapDetection.dto.transaction.BillItemRequestDTO;
import com.scrapDetection.dto.transaction.BillRequestDTO;
import com.scrapDetection.dto.transaction.BillResponseDTO;
import com.scrapDetection.entity.Account;
import com.scrapDetection.entity.Material;
import com.scrapDetection.entity.ScrapYard;
import com.scrapDetection.entity.Transaction;
import com.scrapDetection.entity.TransactionTotal;
import com.scrapDetection.mapper.TransactionMapper;
import com.scrapDetection.repository.AccountRepository;
import com.scrapDetection.repository.MaterialRepository;
import com.scrapDetection.repository.TransactionRepository;
import com.scrapDetection.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private MaterialRepository materialRepository;
    @Mock
    private AccountService accountService;
    @Mock
    private TransactionMapper transactionMapper;

    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionServiceImpl(
                accountRepository,
                transactionRepository,
                materialRepository,
                accountService,
                transactionMapper
        );
    }

    @Test
    void createBill_savesAllItemsWithOneBillIdAndReturnsTotals() {
        ScrapYard yard = ScrapYard.builder().yardId(10L).build();
        Account staff = Account.builder()
                .accountId(20L)
                .accountName("Nhan vien")
                .scrapYard(yard)
                .build();
        Account customer = Account.builder()
                .accountId(1L)
                .accountName("Khach vang lai")
                .build();
        Material iron = material(30L, "Sat", 5_000D, 10D, yard);
        Material copper = material(31L, "Dong", 100_000D, 2D, yard);
        BillRequestDTO request = BillRequestDTO.builder()
                .transactionType("buy")
                .items(List.of(
                        BillItemRequestDTO.builder()
                                .materialId(30L)
                                .weight(2D)
                                .pricePerKg(6_000D)
                                .build(),
                        BillItemRequestDTO.builder()
                                .materialId(31L)
                                .weight(1.5D)
                                .build()
                ))
                .build();

        when(accountService.getCurrentUser()).thenReturn(staff);
        when(accountRepository.getReferenceById(1L)).thenReturn(customer);
        when(materialRepository.findById(30L)).thenReturn(Optional.of(iron));
        when(materialRepository.findById(31L)).thenReturn(Optional.of(copper));
        AtomicLong ids = new AtomicLong(100L);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            if (transaction.getTransactionId() == null) {
                transaction.setTransactionId(ids.getAndIncrement());
                transaction.setCreatedAt(LocalDateTime.of(2026, 8, 7, 10, 30));
            }
            return transaction;
        });

        BillResponseDTO response = transactionService.createBill(request);

        assertNotNull(response.getBillId());
        assertEquals(2, response.getItems().size());
        assertEquals("buy", response.getTransactionType());
        assertEquals(3.5D, response.getTotalWeight());
        assertEquals(162_000D, response.getTotalWorth());
        assertEquals(12D, iron.getStock());
        assertEquals(3.5D, copper.getStock());
        ArgumentCaptor<Transaction> savedTransactions = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(4)).save(savedTransactions.capture());
        assertEquals(1, savedTransactions.getAllValues().stream()
                .map(Transaction::getBillId)
                .distinct()
                .count());
        assertEquals(response.getBillId(), savedTransactions.getValue().getBillId());
    }

    @Test
    void getBillsByYard_groupsNewRowsAndKeepsLegacyRowsSeparate() {
        ScrapYard yard = ScrapYard.builder().yardId(10L).build();
        Account staff = Account.builder().accountName("Nhan vien").build();
        Account customer = Account.builder().accountName("Khach").build();
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 7, 9, 0);
        Transaction iron = transaction(100L, "bill-a", material(30L, "Sat", 5_000D, 0D, yard), staff, customer, 2D, 10_000D, createdAt);
        Transaction copper = transaction(101L, "bill-a", material(31L, "Dong", 90_000D, 0D, yard), staff, customer, 1D, 90_000D, createdAt);
        Transaction legacy = transaction(99L, null, material(32L, "Giay", 2_000D, 0D, yard), staff, customer, 3D, 6_000D, createdAt.minusMinutes(10));
        when(transactionRepository.findByMaterialScrapYardYardId(10L))
                .thenReturn(List.of(legacy, copper, iron));

        List<BillResponseDTO> response = transactionService.getBillsByYard(10L);

        assertEquals(2, response.size());
        assertEquals("bill-a", response.get(0).getBillId());
        assertEquals(2, response.get(0).getItems().size());
        assertEquals(100_000D, response.get(0).getTotalWorth());
        assertEquals("legacy-99", response.get(1).getBillId());
        assertEquals(1, response.get(1).getItems().size());
    }

    private Material material(Long id, String name, double price, double stock, ScrapYard yard) {
        return Material.builder()
                .materialId(id)
                .itemName(name)
                .itemPrice(price)
                .stock(stock)
                .scrapYard(yard)
                .build();
    }

    private Transaction transaction(
            Long id,
            String billId,
            Material material,
            Account staff,
            Account customer,
            double weight,
            double totalWorth,
            LocalDateTime createdAt
    ) {
        Transaction transaction = Transaction.builder()
                .transactionId(id)
                .billId(billId)
                .material(material)
                .createdBy(staff)
                .customer(customer)
                .weight(weight)
                .createdAt(createdAt)
                .build();
        transaction.setTransactionTotal(TransactionTotal.builder()
                .transaction(transaction)
                .totalWorth(totalWorth)
                .build());
        return transaction;
    }
}
