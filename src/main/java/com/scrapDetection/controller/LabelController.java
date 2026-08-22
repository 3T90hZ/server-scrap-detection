package com.scrapDetection.controller;

import com.scrapDetection.dto.label.LabelRequest;
import com.scrapDetection.dto.label.LabelResponse;
import com.scrapDetection.service.LabelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/labels")
@RequiredArgsConstructor
@PreAuthorize("hasRole('YARD_OWNER')")
public class LabelController {

    private final LabelService labelService;

    @PostMapping
    public ResponseEntity<LabelResponse> createLabel(
            @Valid @RequestBody LabelRequest request
    ) {
        LabelResponse response = labelService.createLabel(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<LabelResponse>> getAllLabelsByYard() {
        return ResponseEntity.ok(
                labelService.getAllLabelsByYard()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<LabelResponse> updateLabel(
            @PathVariable Long id,
            @Valid @RequestBody LabelRequest request
    ) {
        LabelResponse response = labelService.updateLabel(request, id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLabel(
            @PathVariable Long id
    ) {
        labelService.deleteLabel(id);
        return ResponseEntity.noContent().build();
    }
}