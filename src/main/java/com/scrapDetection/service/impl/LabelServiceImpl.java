package com.scrapDetection.service.impl;

import com.scrapDetection.dto.label.LabelRequest;
import com.scrapDetection.dto.label.LabelResponse;
import com.scrapDetection.entity.Label;
import com.scrapDetection.entity.Material;
import com.scrapDetection.exception.InvalidRequestException;
import com.scrapDetection.exception.ResourceAlreadyExistsException;
import com.scrapDetection.exception.ResourceNotFoundException;
import com.scrapDetection.mapper.LabelMapper;
import com.scrapDetection.repository.LabelRepository;
import com.scrapDetection.repository.MaterialRepository;
import com.scrapDetection.service.AccountService;
import com.scrapDetection.service.LabelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LabelServiceImpl implements LabelService {

    private final LabelRepository labelRepository;
    private final AccountService accountService;
    private final LabelMapper labelMapper;
    private final MaterialRepository materialRepository;
    @Override
    public LabelResponse createLabel(LabelRequest request) {
        validateLabelRequest(request, null);
        Label label = Label.builder()
                .label(request.getLabelName())
                .material(materialRepository.getReferenceById(request.getMaterialId()))
                .scrapYard(accountService.getCurrentUser().getScrapYard())
                .build();
        return labelMapper.toResponseDTO(labelRepository.save(label));
    }


    @Override
    @Transactional(readOnly = true)
    public List<LabelResponse> getAllLabelsByYard() {
        Long yardId = accountService.getCurrentUser().getScrapYard().getYardId();
        List<Label> labels = labelRepository.findByScrapYardYardId(yardId);
        return labelMapper.toResponseDTOList(labels);
    }

    @Override
    public LabelResponse updateLabel(LabelRequest request, Long id) {
        Label existingLabel = labelRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Label", id));

        if (!accountService.getCurrentUser()
                .getScrapYard()
                .equals(existingLabel.getScrapYard())) {

            throw new InvalidRequestException(
                    "Not allowed to update this label"
            );
        }

        validateLabelRequest(request, existingLabel.getLabelId());
        existingLabel.setLabel(request.getLabelName());
        existingLabel.setMaterial(materialRepository.getReferenceById(request.getMaterialId()));

        return labelMapper.toResponseDTO(labelRepository.save(existingLabel));
    }

    @Override
    public void deleteLabel(Long labelId) {
        Label existingLabel = labelRepository.findById(labelId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Label", labelId));
        if(!accountService.getCurrentUser().getScrapYard().equals(existingLabel.getScrapYard())) {
            throw new InvalidRequestException("Not allowed to delete this label");
        }
        labelRepository.delete(existingLabel);
    }

    private void validateLabelRequest(LabelRequest request, Long excludeLabelId) {

        Long yardId = accountService.getCurrentUser().getScrapYard().getYardId();
        Material material = materialRepository.findById(request.getMaterialId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Material",
                                request.getMaterialId()
                        ));

        if (!material.getScrapYard().getYardId().equals(yardId)) {
            throw new InvalidRequestException(
                    "Material does not belong to your scrap yard"
            );
        }
        labelRepository.findByScrapYardYardIdAndLabel(yardId, request.getLabelName())
                .filter(label -> !label.getLabelId().equals(excludeLabelId))
                .ifPresent(label -> {
                    throw new ResourceAlreadyExistsException("Label already exists");
                });

        labelRepository.findByScrapYardYardIdAndMaterialMaterialId(yardId, request.getMaterialId())
                .filter(label -> !label.getLabelId().equals(excludeLabelId))
                .ifPresent(label -> {
                    throw new ResourceAlreadyExistsException(
                            "Label with this material already exists"
                    );
                });

        if (!materialRepository.existsById(request.getMaterialId())) {
            throw new ResourceNotFoundException("Material", request.getMaterialId());
        }
    }
}