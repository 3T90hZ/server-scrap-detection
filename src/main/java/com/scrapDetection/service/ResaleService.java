package com.scrapDetection.service;

import com.scrapDetection.dto.resale.ResaleRequestDTO;
import com.scrapDetection.dto.resale.ResaleResponseDTO;
import com.scrapDetection.dto.resale.ResaleSummaryDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface ResaleService {

    ResaleResponseDTO createResale(ResaleRequestDTO requestDTO);

    ResaleResponseDTO getResaleById(Long resaleId);

    List<ResaleSummaryDTO> getResalesByYard(Long yardId);

    List<ResaleSummaryDTO> getResalesByStaff(Long staffId);

    List<ResaleSummaryDTO> getResaleSummaries();

    List<ResaleResponseDTO> getResalesByDateRange(LocalDateTime start, LocalDateTime end);
}