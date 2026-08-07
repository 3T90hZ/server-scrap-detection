package com.scrapDetection.service.impl;

import com.scrapDetection.dto.transaction.BillItemRequestDTO;
import com.scrapDetection.dto.transaction.BillItemResponseDTO;
import com.scrapDetection.dto.transaction.BillRequestDTO;
import com.scrapDetection.dto.transaction.BillResponseDTO;
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
import com.scrapDetection.repository.AccountRepository;
import com.scrapDetection.repository.MaterialRepository;
import com.scrapDetection.repository.TransactionRepository;
import com.scrapDetection.service.AccountService;
import com.scrapDetection.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final MaterialRepository materialRepository;
    private final AccountService accountService;
    private final TransactionMapper transactionMapper;

    @Override
    public TransactionResponseDTO createTransaction(TransactionRequestDTO requestDTO) {
        Account currentUser = accountService.getCurrentUser();
        Account customer = resolveCustomer(requestDTO.getCustomerId());
        BillItemRequestDTO item = BillItemRequestDTO.builder()
                .materialId(requestDTO.getMaterialId())
                .weight(requestDTO.getWeight())
                .build();
        Transaction savedTransaction = saveBillItem(currentUser, customer, UUID.randomUUID().toString(), "buy", item);
        return transactionMapper.toResponseDTO(savedTransaction);
    }

    @Override
    public BillResponseDTO createBill(BillRequestDTO requestDTO) {
        Account currentUser = accountService.getCurrentUser();
        Account customer = resolveCustomer(requestDTO.getCustomerId());
        String billId = UUID.randomUUID().toString();
        String transactionType = requestDTO.getTransactionType() != null
                ? requestDTO.getTransactionType().toLowerCase()
                : "buy";

        List<Transaction> transactions = requestDTO.getItems().stream()
                .map(item -> saveBillItem(currentUser, customer, billId, transactionType, item))
                .toList();

        return toBillResponse(billId, transactions);
    }

    @Override
    public List<BillResponseDTO> getBillsByYard(Long yardId) {
        return groupBills(transactionRepository.findByMaterialScrapYardYardId(yardId));
    }

    @Override
    public List<BillResponseDTO> getBillsByStaff(Long staffId) {
        return groupBills(transactionRepository.findByCreated_byAccountId(staffId));
    }

    @Override
    public List<BillResponseDTO> getAllBills() {
        return groupBills(transactionRepository.findAll());
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

    private Account resolveCustomer(Long customerId) {
        return accountRepository.getReferenceById(customerId != null ? customerId : 1L);
    }

    private Transaction saveBillItem(
            Account currentUser,
            Account customer,
            String billId,
            String transactionType,
            BillItemRequestDTO item
    ) {
        Material material = materialRepository.findById(item.getMaterialId())
                .orElseThrow(() -> new ResourceNotFoundException("Material", item.getMaterialId()));

        if (currentUser.getScrapYard() == null
                || !material.getScrapYard().getYardId().equals(currentUser.getScrapYard().getYardId())) {
            throw new InvalidRequestException("You can only create transactions for materials in your yard");
        }

        if ("sell".equals(transactionType) && material.getStock() < item.getWeight()) {
            throw new InvalidRequestException("Insufficient stock for material " + material.getItemName());
        }

        Transaction transaction = Transaction.builder()
                .billId(billId)
                .transactionType(transactionType)
                .material(material)
                .customer(customer)
                .createdBy(currentUser)
                .weight(item.getWeight())
                .build();
        Transaction savedTransaction = transactionRepository.save(transaction);

        if ("sell".equals(transactionType)) {
            material.setStock(material.getStock() - item.getWeight());
        } else {
            material.setStock(material.getStock() + item.getWeight());
        }
        materialRepository.save(material);

        double pricePerKg = item.getPricePerKg() != null
                ? item.getPricePerKg()
                : material.getItemPrice();
        TransactionTotal total = TransactionTotal.builder()
                .transaction(savedTransaction)
                .totalWorth(item.getWeight() * pricePerKg)
                .build();
        savedTransaction.setTransactionTotal(total);
        return transactionRepository.save(savedTransaction);
    }

    private List<BillResponseDTO> groupBills(List<Transaction> transactions) {
        Comparator<Transaction> newestFirst = Comparator.comparing(
                Transaction::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())
        );
        Map<String, List<Transaction>> grouped = transactions.stream()
                .sorted(newestFirst)
                .collect(Collectors.groupingBy(
                        this::effectiveBillId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return grouped.entrySet().stream()
                .map(entry -> toBillResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    private String effectiveBillId(Transaction transaction) {
        return transaction.getBillId() != null
                ? transaction.getBillId()
                : "legacy-" + transaction.getTransactionId();
    }

    private BillResponseDTO toBillResponse(String billId, List<Transaction> transactions) {
        Transaction first = transactions.get(0);
        List<BillItemResponseDTO> items = transactions.stream()
                .map(this::toBillItemResponse)
                .toList();

        return BillResponseDTO.builder()
                .billId(billId)
                .transactionType(first.getTransactionType() != null ? first.getTransactionType() : "buy")
                .items(items)
                .totalWeight(items.stream().mapToDouble(BillItemResponseDTO::getWeight).sum())
                .totalWorth(items.stream().mapToDouble(BillItemResponseDTO::getTotalWorth).sum())
                .customerName(first.getCustomer().getAccountName())
                .createdBy(first.getCreatedBy() != null ? first.getCreatedBy().getAccountName() : null)
                .createdAt(first.getCreatedAt())
                .build();
    }

    private BillItemResponseDTO toBillItemResponse(Transaction transaction) {
        double totalWorth = transaction.getTransactionTotal() != null
                ? transaction.getTransactionTotal().getTotalWorth()
                : 0D;
        double pricePerKg = transaction.getWeight() > 0
                ? totalWorth / transaction.getWeight()
                : 0D;

        return BillItemResponseDTO.builder()
                .transactionId(transaction.getTransactionId())
                .materialId(transaction.getMaterial().getMaterialId())
                .itemName(transaction.getMaterial().getItemName())
                .weight(transaction.getWeight())
                .pricePerKg(pricePerKg)
                .totalWorth(totalWorth)
                .build();
    }


}
