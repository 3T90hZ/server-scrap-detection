package com.scrapDetection.service.impl;

import com.scrapDetection.dto.statistics.MaterialBreakdownDTO;
import com.scrapDetection.dto.statistics.OwnerProfitResponseDTO;
import com.scrapDetection.dto.statistics.StaffSummaryResponseDTO;
import com.scrapDetection.entity.Bill;
import com.scrapDetection.entity.Resale;
import com.scrapDetection.entity.Transaction;
import com.scrapDetection.exception.InvalidRequestException;
import com.scrapDetection.repository.BillRepository;
import com.scrapDetection.repository.ResaleRepository;
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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@Transactional(readOnly = true)
public class StatisticsServiceImpl implements StatisticsService {

    private final BillRepository billRepository;
    private final ResaleRepository resaleRepository;
    private final Clock clock;

    @Autowired
    public StatisticsServiceImpl(
            BillRepository billRepository,
            @Value("${recyclick.statistics.estimated-margin-rate:0.20}") double estimatedMarginRate, ResaleRepository resaleRepository
    ) {
        this(billRepository, Clock.systemDefaultZone(), estimatedMarginRate, resaleRepository);
    }

    // package-private constructor for tests
    StatisticsServiceImpl(
            BillRepository billRepository,
            Clock clock,
            double estimatedMarginRate, ResaleRepository resaleRepository
    ) {
        this.resaleRepository = resaleRepository;
        if (estimatedMarginRate < 0D) {
            throw new IllegalArgumentException("Estimated margin rate must not be negative");
        }
        this.billRepository = billRepository;
        this.clock = clock;
    }

    @Override
    public StaffSummaryResponseDTO getStaffDailySummary(Long staffId) {
        LocalDate today = LocalDate.now(clock);
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime nextDay = today.plusDays(1).atStartOfDay();

        List<Bill> bills = billRepository.findStaffBillsForStatistics(
                staffId,
                startOfDay,
                nextDay
        );

        double totalSpent = 0D;
        double totalWeight = 0D;
        int transactionCount = 0;               // number of line items
        Map<String, MaterialBreakdownDTO> breakdownByMaterial = new TreeMap<>();

        for (Bill bill : bills) {
            for (Transaction tx : bill.getTransactions()) {
                double weight = tx.getWeight() != null ? tx.getWeight() : 0D;
                double worth = getLineWorth(tx);
                String materialName = getMaterialName(tx);

                totalWeight += weight;
                totalSpent += worth;
                transactionCount++;

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
        }

        return StaffSummaryResponseDTO.builder()
                .todayTotalSpent(totalSpent)
                .todayTotalWeight(totalWeight)
                .todayTransactionsCount(transactionCount)   // or bills.size() if you prefer bill count
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

        // ----- Cost (money paid to customers) -----
        List<Bill> bills = billRepository.findYardBillsForStatistics(
                yardId, localStartDate, localEndDate);

        double totalSpent = bills.stream()
                .flatMap(b -> b.getTransactions().stream())
                .mapToDouble(this::getLineWorth)
                .sum();

        // ----- Revenue (money received from factories) -----
        List<Resale> resales = resaleRepository.findYardResalesForStatistics(
                yardId, localStartDate, localEndDate);

        double totalRevenue = resales.stream()
                .mapToDouble(this::getResaleWorth)
                .sum();

        // ----- Real profit -----
        double totalProfit = totalRevenue - totalSpent;

        return OwnerProfitResponseDTO.builder()
                .totalSpent(totalSpent)
                .estimatedRevenue(totalRevenue)   // now real revenue
                .totalProfit(totalProfit)
                .build();
    }

    // ==================== helpers ====================

    private LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, clock.getZone());
    }

    private double getLineWorth(Transaction tx) {
        return tx.getLineWorth() != null ? tx.getLineWorth() : 0D;
    }

    private String getMaterialName(Transaction tx) {
        if (tx.getMaterial() == null || tx.getMaterial().getItemName() == null) {
            return "Unknown";
        }
        return tx.getMaterial().getItemName();
    }
    private double getResaleWorth(Resale resale) {
        if (resale.getResaleTotal() != null && resale.getResaleTotal().getTotalWorth() != null) {
            return resale.getResaleTotal().getTotalWorth();
        }
        // fallback if total is missing (should not happen)
        if (resale.getUnitPrice() != null && resale.getWeight() != null) {
            return resale.getUnitPrice() * resale.getWeight();
        }
        return 0D;
    }
}