package com.scrapDetection.service.impl;

import com.scrapDetection.dto.statistics.MaterialBreakdownDTO;
import com.scrapDetection.dto.statistics.OwnerProfitResponseDTO;
import com.scrapDetection.dto.statistics.StaffSummaryResponseDTO;
import com.scrapDetection.entity.Transaction;
import com.scrapDetection.exception.InvalidRequestException;
import com.scrapDetection.repository.TransactionRepository;
import com.scrapDetection.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

@Service
@Transactional(readOnly = true)
public class StatisticsServiceImpl implements StatisticsService {

    private final TransactionRepository transactionRepository;
    private final Clock clock;
    private final double estimatedMarginRate;

    @Autowired
    public StatisticsServiceImpl(
            TransactionRepository transactionRepository,
            @Value("${recyclick.statistics.estimated-margin-rate:0.20}") double estimatedMarginRate
    ) {
        this(transactionRepository, Clock.systemDefaultZone(), estimatedMarginRate);
    }

    StatisticsServiceImpl(
            TransactionRepository transactionRepository,
            Clock clock,
            double estimatedMarginRate
    ) {
        if (estimatedMarginRate < 0D) {
            throw new IllegalArgumentException("Estimated margin rate must not be negative");
        }
        this.transactionRepository = transactionRepository;
        this.clock = clock;
        this.estimatedMarginRate = estimatedMarginRate;
    }

    @Override
    public StaffSummaryResponseDTO getStaffDailySummary(Long staffId) {
        LocalDate today = LocalDate.now(clock);
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime nextDay = today.plusDays(1).atStartOfDay();

        var transactions = transactionRepository.findStaffTransactionsForStatistics(
                staffId,
                startOfDay,
                nextDay
        );

        double totalSpent = 0D;
        double totalWeight = 0D;
        Map<String, MaterialBreakdownDTO> breakdownByMaterial = new TreeMap<>();

        for (Transaction transaction : transactions) {
            double weight = transaction.getWeight() != null ? transaction.getWeight() : 0D;
            double worth = getTransactionWorth(transaction);
            String materialName = getMaterialName(transaction);

            totalWeight += weight;
            totalSpent += worth;

            MaterialBreakdownDTO breakdown = breakdownByMaterial.computeIfAbsent(
                    materialName,
                    name -> MaterialBreakdownDTO.builder()
                            .materialName(name)
                            .totalWeight(0D)
                            .totalSpent(0D)
                            .build()
            );
            breakdown.setTotalWeight(breakdown.getTotalWeight() + weight);
            breakdown.setTotalSpent(breakdown.getTotalSpent() + worth);
        }

        return StaffSummaryResponseDTO.builder()
                .todayTotalSpent(totalSpent)
                .todayTotalWeight(totalWeight)
                .todayTransactionsCount(transactions.size())
                .materialBreakdown(new ArrayList<>(breakdownByMaterial.values()))
                .build();
    }

    @Override
    public OwnerProfitResponseDTO getOwnerProfitStatistics(
            Long yardId,
            Instant startDate,
            Instant endDate
    ) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new InvalidRequestException("Start date must not be after end date");
        }

        LocalDateTime localStartDate = toLocalDateTime(startDate);
        LocalDateTime localEndDate = toLocalDateTime(endDate);
        var transactions = transactionRepository.findYardTransactionsForStatistics(
                yardId,
                localStartDate,
                localEndDate
        );

        double totalSpent = transactions.stream()
                .mapToDouble(this::getTransactionWorth)
                .sum();
        double totalProfit = totalSpent * estimatedMarginRate;
        double estimatedRevenue = totalSpent + totalProfit;

        return OwnerProfitResponseDTO.builder()
                .totalSpent(totalSpent)
                .estimatedRevenue(estimatedRevenue)
                .totalProfit(totalProfit)
                .build();
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, clock.getZone());
    }

    private double getTransactionWorth(Transaction transaction) {
        if (transaction.getLineWorth() == null ) {
            return 0D;
        }
        return transaction.getLineWorth();
    }

    private String getMaterialName(Transaction transaction) {
        if (transaction.getMaterial() == null || transaction.getMaterial().getItemName() == null) {
            return "Unknown";
        }
        return transaction.getMaterial().getItemName();
    }
}
