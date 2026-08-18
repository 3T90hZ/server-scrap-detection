package com.scrapDetection.service.impl;

import com.scrapDetection.dto.bill.BillItemRequestDTO;
import com.scrapDetection.dto.bill.BillRequestDTO;
import com.scrapDetection.dto.bill.BillResponseDTO;
import com.scrapDetection.dto.bill.BillSummaryDTO;
import com.scrapDetection.entity.Account;
import com.scrapDetection.entity.Bill;
import com.scrapDetection.entity.Material;
import com.scrapDetection.entity.Transaction;
import com.scrapDetection.exception.InvalidRequestException;
import com.scrapDetection.exception.ResourceNotFoundException;
import com.scrapDetection.mapper.BillMapper;
import com.scrapDetection.repository.AccountRepository;
import com.scrapDetection.repository.BillRepository;
import com.scrapDetection.repository.MaterialRepository;
import com.scrapDetection.service.AccountService;
import com.scrapDetection.service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;
    private final MaterialRepository materialRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final BillMapper billMapper;

    @Override
    public BillResponseDTO createBill(BillRequestDTO requestDTO) {
        Account currentUser = accountService.getCurrentUser();

        // Resolve customer (fallback to id=1 for detection API compatibility)
        Account customer;
        if (requestDTO.getCustomerId() != null) {
            customer = accountRepository.findById(requestDTO.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", requestDTO.getCustomerId()));
        } else {
            customer = accountRepository.getReferenceById(1L);
        }

        Bill bill = Bill.builder()
                .customer(customer)
                .createdBy(currentUser)
                .totalWorth(0.0) // will be calculated
                .build();

        double totalWorth = 0.0;

        for (BillItemRequestDTO itemDto : requestDTO.getItems()) {
            Material material = materialRepository.findById(itemDto.getMaterialId())
                    .orElseThrow(() -> new ResourceNotFoundException("Material", itemDto.getMaterialId()));

            // Security: material must belong to the same yard
            if (!material.getScrapYard().getYardId().equals(currentUser.getScrapYard().getYardId())) {
                throw new InvalidRequestException(
                        "You can only create bills for materials in your yard (materialId=" + itemDto.getMaterialId() + ")");
            }

            double lineWorth = itemDto.getWeight() * material.getItemPrice();

            Transaction transaction = Transaction.builder()
                    .material(material)
                    .weight(itemDto.getWeight())
                    .lineWorth(lineWorth)
                    .build();

            bill.addTransaction(transaction);
            totalWorth += lineWorth;

            // Update stock
            material.setStock(material.getStock() + itemDto.getWeight());
            materialRepository.save(material);
        }

        bill.setTotalWorth(totalWorth);

        Bill savedBill = billRepository.save(bill);
        return billMapper.toResponseDTO(savedBill);
    }

    @Override
    public BillResponseDTO getBillById(Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", billId));
        return billMapper.toResponseDTO(bill);
    }

    @Override
    public List<BillSummaryDTO> getBillsByCustomer(Long customerId) {
        return billMapper.toSummaryDTOList(billRepository.findByCustomerAccountId(customerId));
    }

    @Override
    public List<BillSummaryDTO> getBillsByYard(Long yardId) {
        return billMapper.toSummaryDTOList(billRepository.findByYardId(yardId));
    }

    @Override
    public List<BillSummaryDTO> getBillsByStaff(Long staffId) {
        return billMapper.toSummaryDTOList(billRepository.findByCreatedByAccountId(staffId));
    }

    @Override
    public List<BillSummaryDTO> getBillSummaries() {
        return billMapper.toSummaryDTOList(billRepository.findAll());
    }

    @Override
    public List<BillResponseDTO> getBillsByDateRange(LocalDateTime start, LocalDateTime end) {
        return billMapper.toResponseDTOList(billRepository.findByCreatedAtBetween(start, end));
    }
}