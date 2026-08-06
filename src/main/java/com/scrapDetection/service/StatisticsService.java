package com.scrapDetection.service;

import com.scrapDetection.dto.statistics.OwnerProfitResponseDTO;
import com.scrapDetection.dto.statistics.StaffSummaryResponseDTO;

import java.time.Instant;

public interface StatisticsService {

    StaffSummaryResponseDTO getStaffDailySummary(Long staffId);

    OwnerProfitResponseDTO getOwnerProfitStatistics(Long yardId, Instant startDate, Instant endDate);
}
