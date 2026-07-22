package com.scrapDetection.controller;

import com.scrapDetection.dto.scrapyard.ScrapYardRequestDTO;
import com.scrapDetection.dto.scrapyard.ScrapYardResponseDTO;
import com.scrapDetection.dto.scrapyard.ScrapYardStatusRequestDTO;
import com.scrapDetection.dto.scrapyard.ScrapYardUpdateRequestDTO;
import com.scrapDetection.service.ScrapYardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scrap-yards")
@RequiredArgsConstructor
public class ScrapYardController {

    private final ScrapYardService scrapYardService;

    // CREATE - Create a new scrapyard

    @PostMapping("/request")
    public ResponseEntity<ScrapYardResponseDTO> createScrapYardRequest(
            @Valid @RequestBody ScrapYardRequestDTO requestDTO) {
        ScrapYardResponseDTO response = scrapYardService.createScrapYardRequest(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // READ - Get scrapyard by ID
    @PreAuthorize("hasAnyRole('YARD_OWNER','ADMIN')")
    @GetMapping("/{yardId}")
    public ResponseEntity<ScrapYardResponseDTO> getScrapYardById(@PathVariable Long yardId) {
        System.out.println("yardId:" + yardId);
        ScrapYardResponseDTO response = scrapYardService.getScrapYardById(yardId);
        return ResponseEntity.ok(response);
    }

    // READ - Get all scrapyards(PUBLIC)
    @GetMapping
    public ResponseEntity<Page<ScrapYardResponseDTO>> getAllActiveScrapYards(Pageable pageable) {
        Page<ScrapYardResponseDTO> response = scrapYardService.getAllActiveScrapYards(pageable);
        return ResponseEntity.ok(response);
    }

    // READ - Get all ScrapYards with Pagination
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/paged")
    public ResponseEntity<Page<ScrapYardResponseDTO>> getAllScrapYardsPaged(Pageable pageable) {
        Page<ScrapYardResponseDTO> response = scrapYardService.getAllScrapYards(pageable);
        return ResponseEntity.ok(response);
    }

    // READ - Get scrapyards by Status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ScrapYardResponseDTO>> getScrapYardsByStatus(@PathVariable String status) {
        List<ScrapYardResponseDTO> response = scrapYardService.getScrapYardsByStatus(status);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('YARD_OWNER')")
    @PutMapping("/{yardId}/update")
    public ResponseEntity<ScrapYardResponseDTO> updateScrapYard(
            @PathVariable Long yardId,
            @Valid @RequestBody ScrapYardUpdateRequestDTO requestDTO) {

        ScrapYardResponseDTO response = scrapYardService.updateScrapYard(yardId, requestDTO);
        return ResponseEntity.ok(response);
    }

    // UPDATE - Update scrapyard status
    @PreAuthorize("hasAnyRole('YARD_OWNER','ADMIN')")
    @PutMapping("/{yardId}/status")
    public ResponseEntity<ScrapYardResponseDTO> updateScrapYardStatus(
            @Valid @RequestBody ScrapYardStatusRequestDTO requestDTO, @PathVariable Long yardId) {

        ScrapYardResponseDTO response = scrapYardService.updateScrapYardStatus(requestDTO, yardId);
        return ResponseEntity.ok(response);
    }

    // Find ScrapYard by name
    @GetMapping("/name/{yardName}")
    public ResponseEntity<ScrapYardResponseDTO> getScrapYardByName(@PathVariable String yardName) {
        ScrapYardResponseDTO response = scrapYardService.getScrapYardByName(yardName);
        return ResponseEntity.ok(response);
    }

    // Find ScrapYard by name (partial match)
    @GetMapping("/search")
    public ResponseEntity<List<ScrapYardResponseDTO>> searchScrapYards(
            @RequestParam String name) {

        List<ScrapYardResponseDTO> response = scrapYardService.searchScrapYardsByName(name);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE - Delete scrapyard
     */
    @PreAuthorize("hasAnyRole('YARD_OWNER','ADMIN')")
    @DeleteMapping("/{yardId}/delete")
    public ResponseEntity<Void> deleteScrapYard(@PathVariable Long yardId) {
        scrapYardService.deleteScrapYard(yardId);
        return ResponseEntity.noContent().build();
    }
}