package com.scrapDetection.service.impl;

import com.scrapDetection.dto.resale.ResaleRequestDTO;
import com.scrapDetection.dto.resale.ResaleResponseDTO;
import com.scrapDetection.dto.resale.ResaleSummaryDTO;
import com.scrapDetection.entity.*;
import com.scrapDetection.exception.InvalidRequestException;
import com.scrapDetection.exception.ResourceNotFoundException;
import com.scrapDetection.mapper.ResaleMapper;
import com.scrapDetection.repository.MaterialRepository;
import com.scrapDetection.repository.ResaleRepository;
import com.scrapDetection.service.CurrentUserService;
import com.scrapDetection.service.ResaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ResaleServiceImpl implements ResaleService {

    private final ResaleRepository resaleRepository;
    private final MaterialRepository materialRepository;
    private final ResaleMapper resaleMapper;
    private final CurrentUserService currentUserService;

    @Override
    public ResaleResponseDTO createResale(ResaleRequestDTO requestDTO) {
        Account currentUser = currentUserService.getCurrentUser();

        Material material = materialRepository.findById(requestDTO.getMaterialId())
                .orElseThrow(() -> new ResourceNotFoundException("Material", requestDTO.getMaterialId()));

        if (!material.getScrapYard().getYardId().equals(currentUser.getScrapYard().getYardId())) {
            throw new InvalidRequestException("You can only create resales for materials in your yard");
        }

        Double currentStock = material.getStock();
        if (currentStock == null) {
            currentStock = 0.0;
        }

        if (currentStock < requestDTO.getWeight()) {
            throw new InvalidRequestException("Insufficient stock. Available: " + currentStock);
        }

        Resale resale = resaleMapper.toEntity(requestDTO);
        resale.setMaterial(material);
        resale.setCreatedBy(currentUser);

        // Set total before save so cascade persists it in one roundtrip
        ResaleTotal total = new ResaleTotal();
        total.setResale(resale);
        total.setTotalWorth(requestDTO.getWeight() * requestDTO.getUnitPrice());
        resale.setResaleTotal(total);

        Resale savedResale = resaleRepository.save(resale);

        // Decrease stock
        material.setStock(currentStock - requestDTO.getWeight());
        materialRepository.save(material);

        return resaleMapper.toResponseDTO(savedResale);
    }

    @Override
    public ResaleResponseDTO getResaleById(Long resaleId) {
        Resale resale = resaleRepository.findById(resaleId)
                .orElseThrow(() -> new ResourceNotFoundException("Resale", resaleId));

        Long currentYardId = currentUserService.getCurrentUser().getScrapYard().getYardId();
        Long resaleYardId = resale.getMaterial().getScrapYard().getYardId();
        if (!currentYardId.equals(resaleYardId)) {
            throw new InvalidRequestException("No permission to retrieve resale");
        }
        return resaleMapper.toResponseDTO(resale);
    }

    @Override
    public List<ResaleSummaryDTO> getResalesByYard() {
        List<Resale> resales = resaleRepository.findByMaterialScrapYardYardIdOrderByCreatedAtDesc(currentUserService.getCurrentUser().getScrapYard().getYardId());
        return resaleMapper.toSummaryDTOList(resales);
    }

    @Override
    public List<ResaleResponseDTO> getResalesByDateRange(LocalDateTime start, LocalDateTime end) {
        Account currentUser = currentUserService.getCurrentUser();
        List<Resale> resales = resaleRepository.findByMaterialScrapYardYardIdAndCreatedAtBetweenOrderByCreatedAtDesc(currentUser.getScrapYard().getYardId(), start, end);
        return resaleMapper.toResponseDTOList(resales);
    }
}