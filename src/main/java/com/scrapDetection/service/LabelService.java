package com.scrapDetection.service;

import com.scrapDetection.dto.label.LabelRequest;
import com.scrapDetection.dto.label.LabelResponse;

import java.util.List;

public interface LabelService {
    LabelResponse createLabel(LabelRequest request);

    List<LabelResponse> getAllLabelsByYard();

    LabelResponse updateLabel(LabelRequest request, Long id);

    void deleteLabel(Long labelId);
}
