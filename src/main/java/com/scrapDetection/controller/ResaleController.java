package com.scrapDetection.controller;

import com.scrapDetection.dto.resale.ResaleRequestDTO;
import com.scrapDetection.dto.resale.ResaleResponseDTO;
import com.scrapDetection.dto.resale.ResaleSummaryDTO;
import com.scrapDetection.service.ResaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/resales")
@RequiredArgsConstructor
@PreAuthorize("hasRole('YARD_OWNER')")
public class ResaleController {

    private final ResaleService resaleService;

    /**
     * Create a new resale
     */
    @PostMapping
    public ResponseEntity<ResaleResponseDTO> createResale(
            @Valid @RequestBody ResaleRequestDTO requestDTO) {

        ResaleResponseDTO response = resaleService.createResale(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a single resale by ID
     */
    @GetMapping("/{resaleId}")
    public ResponseEntity<ResaleResponseDTO> getResaleById(
            @PathVariable Long resaleId) {

        return ResponseEntity.ok(resaleService.getResaleById(resaleId));
    }

    /**
     * Get all resales in the current owner's yard (convenience endpoint)
     */
    @GetMapping("/my-yard")
    public ResponseEntity<List<ResaleSummaryDTO>> getMyYardResales() {
        return ResponseEntity.ok(resaleService.getResalesByYard());
    }

    /**
     * Get all resales belonging to a specific yard
     */
    @GetMapping("/yard/{yardId}")
    public ResponseEntity<List<ResaleSummaryDTO>> getResalesByYard() {

        return ResponseEntity.ok(resaleService.getResalesByYard());
    }
    @GetMapping("/date-range")
    public ResponseEntity<List<ResaleResponseDTO>> getResalesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        return ResponseEntity.ok(resaleService.getResalesByDateRange(start, end));
    }
}