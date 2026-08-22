package com.scrapDetection.service;

import com.scrapDetection.dto.bill.BillRequestDTO;
import com.scrapDetection.dto.bill.BillResponseDTO;
import com.scrapDetection.dto.bill.BillSummaryDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface BillService {

    BillResponseDTO createBill(BillRequestDTO requestDTO);

    BillResponseDTO getBillById(Long billId);

    List<BillSummaryDTO> getBillsByCustomer();

    List<BillSummaryDTO> getBillsByYard(Long yardId);

    List<BillSummaryDTO> getBillsByStaff(Long staffId);

    List<BillSummaryDTO> getBillsByDateRange(LocalDateTime start, LocalDateTime end);
}