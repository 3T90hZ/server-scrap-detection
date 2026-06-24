package com.scrapDetection.service.impl;

import com.scrapDetection.dto.detection.DetectionRequestDTO;
import com.scrapDetection.dto.detection.DetectionRequestDTO.DetectionItemDTO;
import com.scrapDetection.dto.detection.DetectionResponseDTO;
import com.scrapDetection.entity.Material;
import com.scrapDetection.entity.Transaction;
import com.scrapDetection.mapper.DetectionMapper;
import com.scrapDetection.repository.MaterialRepository;
import com.scrapDetection.repository.TransactionRepository;
import com.scrapDetection.service.DetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/*
 Logic for Pi detection events.
 V1 version
 1. From the detections list, pick the one with the highest confidence.
 2. Look up the Material whose itemName matches that class_name
    (case-insensitive).
 3. Create a Transaction with:
      - material  → matched Material
      - weight    → weight_g from the payload
      - customer  → null  (nullable in v1; staff/owner confirms later)
 4. Return a DetectionResponse with the new transactionId and unit price.
 Error cases
 - Payload has no detections         → error response (no transaction saved)
 - class_name not in materials table → error response (no transaction saved)
   Both cases return HTTP 200 with status="error" so the Pi can log them
   without crashing. The controller maps them to 422 for clarity.
 */

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional

public class DetectionServiceImpl implements DetectionService {
    private final MaterialRepository materialRepository;
    private final TransactionRepository transactionRepository;
    private final DetectionMapper detectionMapper;

    @Override
    public DetectionResponseDTO processDetection (
            DetectionRequestDTO requestDTO) {

        List<DetectionItemDTO> detections = requestDTO.getDetections();

        // Validate payload
        if (detections == null || detections.isEmpty()) {

            log.warn(
                    "[DetectionService] Payload contained no detections. timestamp={}",
                    requestDTO.getTimestamp());

            return detectionMapper.toErrorResponse(
                    "Payload contained no detections.");
        }

        // Pick the highest confidence detection
        DetectionItemDTO bestDetection = detections.stream()
                .filter(d -> d.getClassName() != null
                        && d.getConfidence() != null)
                .max(Comparator.comparingDouble(
                        DetectionItemDTO::getConfidence))
                .orElse(null);

        if (bestDetection == null) {

            log.warn(
                    "[DetectionService] Detection items missing class_name/confidence");

            return detectionMapper.toErrorResponse(
                    "Detection items had no valid class_name / confidence.");
        }

        log.info(
                "[DetectionService] Best detection: class={} confidence={} weight={}",
                bestDetection.getClassName(),
                bestDetection.getConfidence(),
                requestDTO.getWeightG());

        // Find material
        Optional<Material> materialOpt =
                materialRepository.findFirstByItemNameIgnoreCase(
                        bestDetection.getClassName());

        if (materialOpt.isEmpty()) {

            log.warn(
                    "[DetectionService] No material found for class_name={}",
                    bestDetection.getClassName());

            return detectionMapper.toErrorResponse(
                    "No material found matching class name: "
                            + bestDetection.getClassName());
        }

        Material material = materialOpt.get();

        // Create transaction
        Transaction transaction =
                detectionMapper.toEntity(requestDTO, material);

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        log.info(
                "[DetectionService] Transaction #{} created",
                savedTransaction.getTransactionId());

        return detectionMapper.toSuccessResponse(
                savedTransaction,
                material);
    }
}

