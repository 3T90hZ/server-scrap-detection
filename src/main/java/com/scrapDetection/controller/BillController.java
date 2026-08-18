package com.scrapDetection.controller;

import com.scrapDetection.dto.bill.BillRequestDTO;
import com.scrapDetection.dto.bill.BillResponseDTO;
import com.scrapDetection.dto.bill.BillSummaryDTO;
import com.scrapDetection.service.AccountService;
import com.scrapDetection.service.BillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;
    private final AccountService accountService;

    // Create a bill (one or more materials)
    @PreAuthorize("hasAnyRole('STAFF', 'YARD_OWNER')")
    @PostMapping
    public ResponseEntity<BillResponseDTO> createBill(
            @Valid @RequestBody BillRequestDTO requestDTO) {

        BillResponseDTO response = billService.createBill(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Get a single bill by ID
    @PreAuthorize("hasAnyRole('STAFF', 'YARD_OWNER', 'CUSTOMER')")
    @GetMapping("/{billId}")
    public ResponseEntity<BillResponseDTO> getBillById(@PathVariable Long billId) {
        BillResponseDTO response = billService.getBillById(billId);
        return ResponseEntity.ok(response);
    }

    // Get bills in the current owner's yard
    @PreAuthorize("hasRole('YARD_OWNER')")
    @GetMapping("/my-yard")
    public ResponseEntity<List<BillSummaryDTO>> getMyYardBills() {
        Long yardId = getCurrentUserYardId();
        List<BillSummaryDTO> response = billService.getBillsByYard(yardId);
        return ResponseEntity.ok(response);
    }

    // Get bills created by the current staff
    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/my-bills")
    public ResponseEntity<List<BillSummaryDTO>> getMyBills() {
        Long currentUserId = getCurrentUserId();
        List<BillSummaryDTO> response = billService.getBillsByStaff(currentUserId);
        return ResponseEntity.ok(response);
    }

    // Get all bills of a specific customer
    @PreAuthorize("hasAnyRole('STAFF', 'YARD_OWNER', 'CUSTOMER')")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<BillSummaryDTO>> getBillsByCustomer(@PathVariable Long customerId) {
        List<BillSummaryDTO> response = billService.getBillsByCustomer(customerId);
        return ResponseEntity.ok(response);
    }

    // Get all bill summaries
    @GetMapping("/summary")
    public ResponseEntity<List<BillSummaryDTO>> getBillSummaries() {
        List<BillSummaryDTO> response = billService.getBillSummaries();
        return ResponseEntity.ok(response);
    }

    // Get bills by date range
    @GetMapping("/date-range")
    public ResponseEntity<List<BillResponseDTO>> getBillsByDateRange(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {

        List<BillResponseDTO> response = billService.getBillsByDateRange(start, end);
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