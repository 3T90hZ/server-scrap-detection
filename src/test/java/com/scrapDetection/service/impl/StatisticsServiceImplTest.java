package com.scrapDetection.service.impl;

import com.scrapDetection.dto.statistics.OwnerProfitResponseDTO;
import com.scrapDetection.dto.statistics.StaffSummaryResponseDTO;
import com.scrapDetection.entity.Material;
import com.scrapDetection.entity.Transaction;
import com.scrapDetection.entity.TransactionTotal;
import com.scrapDetection.exception.InvalidRequestException;
import com.scrapDetection.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceImplTest {

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-08-06T03:00:00Z"),
            APP_ZONE
    );

    @Mock
    private TransactionRepository transactionRepository;

    private StatisticsServiceImpl statisticsService;

    @BeforeEach
    void setUp() {
        statisticsService = new StatisticsServiceImpl(transactionRepository, FIXED_CLOCK, 0.20D);
    }

    @Test
    void getStaffDailySummary_aggregatesTodayByMaterial() {
        LocalDateTime startOfDay = LocalDateTime.of(2026, 8, 6, 0, 0);
        LocalDateTime nextDay = LocalDateTime.of(2026, 8, 7, 0, 0);
        when(transactionRepository.findStaffTransactionsForStatistics(7L, startOfDay, nextDay))
                .thenReturn(List.of(
                        transaction("Giay", 2.0D, 100.0D),
                        transaction("Nhua", 3.0D, 150.0D),
                        transaction("Giay", 1.0D, 50.0D)
                ));

        StaffSummaryResponseDTO result = statisticsService.getStaffDailySummary(7L);

        assertEquals(3, result.getTodayTransactionsCount());
        assertEquals(6.0D, result.getTodayTotalWeight());
        assertEquals(300.0D, result.getTodayTotalSpent());
        assertEquals(2, result.getMaterialBreakdown().size());
        assertEquals("Giay", result.getMaterialBreakdown().get(0).getMaterialName());
        assertEquals(3.0D, result.getMaterialBreakdown().get(0).getTotalWeight());
        assertEquals(150.0D, result.getMaterialBreakdown().get(0).getTotalSpent());
        assertEquals("Nhua", result.getMaterialBreakdown().get(1).getMaterialName());
    }

    @Test
    void getOwnerProfitStatistics_startOnly_usesConfiguredMargin() {
        Instant start = Instant.parse("2026-08-01T00:00:00Z");
        LocalDateTime localStart = LocalDateTime.ofInstant(start, APP_ZONE);
        when(transactionRepository.findYardTransactionsForStatistics(3L, localStart, null))
                .thenReturn(List.of(
                        transaction("Sat", 4.0D, 400.0D),
                        transaction("Dong", 1.0D, 100.0D)
                ));

        OwnerProfitResponseDTO result = statisticsService.getOwnerProfitStatistics(3L, start, null);

        assertEquals(500.0D, result.getTotalSpent());
        assertEquals(600.0D, result.getEstimatedRevenue());
        assertEquals(100.0D, result.getTotalProfit());
        verify(transactionRepository).findYardTransactionsForStatistics(3L, localStart, null);
    }

    @Test
    void getOwnerProfitStatistics_startAfterEnd_rejectsBeforeQuery() {
        Instant start = Instant.parse("2026-08-06T00:00:00Z");
        Instant end = Instant.parse("2026-08-05T00:00:00Z");

        assertThrows(
                InvalidRequestException.class,
                () -> statisticsService.getOwnerProfitStatistics(3L, start, end)
        );

        verifyNoInteractions(transactionRepository);
    }

    private Transaction transaction(String materialName, double weight, double totalWorth) {
        Material material = Material.builder()
                .itemName(materialName)
                .build();
        Transaction transaction = Transaction.builder()
                .material(material)
                .weight(weight)
                .build();
        TransactionTotal total = TransactionTotal.builder()
                .transaction(transaction)
                .totalWorth(totalWorth)
                .build();
        transaction.setTransactionTotal(total);
        return transaction;
    }
}
