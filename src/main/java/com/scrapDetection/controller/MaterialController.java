package com.scrapDetection.controller;

import com.scrapDetection.dto.material.MaterialRequestDTO;
import com.scrapDetection.dto.material.MaterialResponseDTO;
import com.scrapDetection.service.AccountService;
import com.scrapDetection.service.MaterialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;
    private final AccountService accountService;   // ← Injected

    /**
     * YARD OWNER - Create New Material
     */
    @PreAuthorize("hasRole('YARD_OWNER')")
    @PostMapping
    public ResponseEntity<MaterialResponseDTO> createMaterial(
            @Valid @RequestBody MaterialRequestDTO requestDTO) {

        MaterialResponseDTO response = materialService.createMaterial(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * YARD OWNER - Update Material
     */
    @PreAuthorize("hasRole('YARD_OWNER')")
    @PutMapping("/{materialId}")
    public ResponseEntity<MaterialResponseDTO> updateMaterial(
            @PathVariable Long materialId,
            @Valid @RequestBody MaterialRequestDTO requestDTO) {

        MaterialResponseDTO response = materialService.updateMaterial(materialId, requestDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * PUBLIC - Get Material by ID
     */
    @GetMapping("/{materialId}")
    public ResponseEntity<MaterialResponseDTO> getMaterialById(@PathVariable Long materialId) {
        MaterialResponseDTO response = materialService.getMaterialById(materialId);
        return ResponseEntity.ok(response);
    }

    /**
     * YARD OWNER - Get All inactive Materials in Their Own Yard
     */
    @PreAuthorize("hasRole('YARD_OWNER')")
    @GetMapping("/my-yard-Inactive")
    public ResponseEntity<List<MaterialResponseDTO>> getMyYardInactiveMaterials() {
        Long yardId = getCurrentUserYardId();
        List<MaterialResponseDTO> materials = materialService.getMaterialsByYardIdAndStatus(yardId, "INACTIVE");
        return ResponseEntity.ok(materials);
    }

    @PreAuthorize("hasAnyRole('YARD_OWNER','STAFF')")
    @GetMapping("/my-yard-active")
    public ResponseEntity<List<MaterialResponseDTO>> getMyYardActiveMaterials() {
        Long yardId = getCurrentUserYardId();
        List<MaterialResponseDTO> materials = materialService.getMaterialsByYardIdAndStatus(yardId, "ACTIVE");
        return ResponseEntity.ok(materials);
    }

    /**
     * PUBLIC - Get Materials by Yard ID (for customers)
     */
    @GetMapping("/yard/{yardId}")
    public ResponseEntity<List<MaterialResponseDTO>> getMaterialsByYard(@PathVariable Long yardId) {
        List<MaterialResponseDTO> materials = materialService.getMaterialsByYardIdAndStatus(yardId, "ACTIVE");
        return ResponseEntity.ok(materials);
    }

    // Search Materials by Name (for customers)
    @GetMapping("/search")
    public ResponseEntity<List<MaterialResponseDTO>> searchMaterials(
            @RequestParam String name) {

        List<MaterialResponseDTO> materials = materialService.searchActiveMaterialsByName(name);
        return ResponseEntity.ok(materials);
    }

    // Search Materials by Name (for yard owner)
    @PreAuthorize("hasAnyRole('YARD_OWNER','STAFF')")
    @GetMapping("/search-in-yard")
    public ResponseEntity<List<MaterialResponseDTO>> searchYardMaterials(
            @RequestParam String name) {
        Long yardId = getCurrentUserYardId();
        List<MaterialResponseDTO> materials = materialService.searchMaterialsInYardByName(yardId, name);
        return ResponseEntity.ok(materials);
    }

    // Get Materials by Status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<MaterialResponseDTO>> getMaterialsByStatus(
            @PathVariable String status) {

        List<MaterialResponseDTO> materials = materialService.getMaterialsByStatus(status);
        return ResponseEntity.ok(materials);
    }

    // YARD OWNER - Delete Material
    @PreAuthorize("hasRole('YARD_OWNER')")
    @DeleteMapping("/{materialId}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long materialId) {
        materialService.deleteMaterial(materialId);
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserYardId() {
        var currentUser = accountService.getCurrentUser();

        if (currentUser.getScrapYard() == null) {
            throw new com.scrapDetection.exception.InvalidRequestException("You are not assigned to any scrap yard");
        }

        return currentUser.getScrapYard().getYardId();
    }
}