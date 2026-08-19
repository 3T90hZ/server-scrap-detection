package com.scrapDetection.controller;

import com.scrapDetection.dto.statistics.OwnerProfitResponseDTO;
import com.scrapDetection.dto.statistics.StaffSummaryResponseDTO;
import com.scrapDetection.exception.InvalidRequestException;
import com.scrapDetection.service.AccountService;
import com.scrapDetection.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final AccountService accountService;

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/staff/summary")
    public ResponseEntity<StaffSummaryResponseDTO> getStaffDailySummary() {
        Long staffId = accountService.getCurrentUser().getAccountId();
        return ResponseEntity.ok(statisticsService.getStaffDailySummary(staffId));
    }

    @PreAuthorize("hasRole('YARD_OWNER')")
    @GetMapping("/owner/profit")
    public ResponseEntity<OwnerProfitResponseDTO> getOwnerProfit(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant endDate
    ) {
        var currentUser = accountService.getCurrentUser();
        if (currentUser.getScrapYard() == null) {
            throw new InvalidRequestException("You are not assigned to any scrap yard");
        }

        return ResponseEntity.ok(statisticsService.getOwnerProfitStatistics(
                currentUser.getScrapYard().getYardId(),
                startDate,
                endDate
        ));
    }
}
