package com.scrapDetection.controller;

import com.scrapDetection.dto.scrapyard.ScrapYardRequestDTO;
import com.scrapDetection.dto.scrapyard.ScrapYardResponseDTO;
import com.scrapDetection.service.ScrapYardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scrap-yards")
@RequiredArgsConstructor
public class ScrapYardController {

    private final ScrapYardService scrapYardService;

    // CREATE - Create a new scrapyard

    @PostMapping
    public ResponseEntity<ScrapYardResponseDTO> createScrapYard(
            @Valid @RequestBody ScrapYardRequestDTO requestDTO) {

        ScrapYardResponseDTO response = scrapYardService.createScrapYard(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // READ - Get scrapyard by ID@GetMapping("/{yardId}")
    public ResponseEntity<ScrapYardResponseDTO> getScrapYardById(@PathVariable Long yardId) {
        ScrapYardResponseDTO response = scrapYardService.getScrapYardById(yardId);
        return ResponseEntity.ok(response);
    }

    // READ - Get all scrapyards
    @GetMapping
    public ResponseEntity<List<ScrapYardResponseDTO>> getAllScrapYards() {
        List<ScrapYardResponseDTO> response = scrapYardService.getAllScrapYards();
        return ResponseEntity.ok(response);
    }

    // READ - Get all ScrapYards with Pagination
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

    // UPDATE - Update scrapyard
    @PutMapping("/{yardId}")
    public ResponseEntity<ScrapYardResponseDTO> updateScrapYard(
            @PathVariable Long yardId,
            @Valid @RequestBody ScrapYardRequestDTO requestDTO) {

        ScrapYardResponseDTO response = scrapYardService.updateScrapYard(yardId, requestDTO);
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
    @DeleteMapping("/{yardId}")
    public ResponseEntity<Void> deleteScrapYard(@PathVariable Long yardId) {
        scrapYardService.deleteScrapYard(yardId);
        return ResponseEntity.noContent().build();
    }
}