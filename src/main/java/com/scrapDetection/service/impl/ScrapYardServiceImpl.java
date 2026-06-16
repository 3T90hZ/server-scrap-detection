package com.scrapDetection.service.impl;

import com.scrapDetection.dto.scrapyard.ScrapYardRequestDTO;
import com.scrapDetection.dto.scrapyard.ScrapYardResponseDTO;
import com.scrapDetection.entity.ScrapYard;
import com.scrapDetection.exception.ResourceAlreadyExistsException;
import com.scrapDetection.exception.ResourceNotFoundException;
import com.scrapDetection.mapper.ScrapYardMapper;
import com.scrapDetection.repository.ScrapYardRepository;
import com.scrapDetection.service.ScrapYardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ScrapYardServiceImpl implements ScrapYardService {

    private final ScrapYardRepository scrapYardRepository;
    private final ScrapYardMapper scrapYardMapper;

    @Override
    public ScrapYardResponseDTO createScrapYard(ScrapYardRequestDTO requestDTO) {
        requestDTO.setPhoneNumbers(requestDTO.getPhoneNumbers().trim());
        if (scrapYardRepository.existsByPhoneNumbers(requestDTO.getPhoneNumbers())) {
            throw new ResourceAlreadyExistsException("Scrap Yard", "phoneNumbers", requestDTO.getPhoneNumbers());
        }

        if(scrapYardRepository.existsByAddress(requestDTO.getAddress())) {
            throw new ResourceAlreadyExistsException("Scrap Yard", "address", requestDTO.getAddress());
        }

        ScrapYard scrapYard = scrapYardMapper.toEntity(requestDTO);

        if (scrapYard.getStatus() == null || scrapYard.getStatus().isBlank()) {
            scrapYard.setStatus("ACTIVE");
        }

        ScrapYard savedYard = scrapYardRepository.save(scrapYard);
        return scrapYardMapper.toResponseDTO(savedYard);
    }

    @Override
    @Transactional(readOnly = true)
    public ScrapYardResponseDTO getScrapYardById(Long yardId) {
        ScrapYard scrapYard = scrapYardRepository.findById(yardId)
                .orElseThrow(() -> new ResourceNotFoundException("Scrap Yard", yardId));

        return scrapYardMapper.toResponseDTO(scrapYard);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScrapYardResponseDTO> getAllScrapYards() {
        List<ScrapYard> yards = scrapYardRepository.findAll();
        return scrapYardMapper.toResponseDTOList(yards);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ScrapYardResponseDTO> getAllScrapYards(Pageable pageable) {
        Page<ScrapYard> yardPage = scrapYardRepository.findAll(pageable);
        return yardPage.map(scrapYardMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScrapYardResponseDTO> getScrapYardsByStatus(String status) {
        List<ScrapYard> yards = scrapYardRepository.findByStatus(status);
        return scrapYardMapper.toResponseDTOList(yards);
    }

    @Override
    public ScrapYardResponseDTO updateScrapYard(Long yardId, ScrapYardRequestDTO requestDTO) {
        ScrapYard existingYard = scrapYardRepository.findById(yardId)
                .orElseThrow(() -> new ResourceNotFoundException("Scrap Yard", yardId));

        requestDTO.setPhoneNumbers(requestDTO.getPhoneNumbers().trim());

        if(scrapYardRepository.existsByAddress(requestDTO.getAddress())) {
            throw new  ResourceAlreadyExistsException("Scrap Yard", "address", requestDTO.getAddress());
        }

        if(scrapYardRepository.existsByPhoneNumbers(requestDTO.getPhoneNumbers())) {
            throw new ResourceNotFoundException("Scrap Yard", "phoneNumbers", requestDTO.getPhoneNumbers());
        }
        // Update entity from DTO
        scrapYardMapper.updateEntityFromDTO(requestDTO, existingYard);

        ScrapYard updatedYard = scrapYardRepository.save(existingYard);
        return scrapYardMapper.toResponseDTO(updatedYard);
    }

    @Override
    public void deleteScrapYard(Long yardId) {
        if (!scrapYardRepository.existsById(yardId)) {
            throw new ResourceNotFoundException("Scrap Yard", yardId);
        }
        scrapYardRepository.deleteById(yardId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long yardId) {
        return scrapYardRepository.existsById(yardId);
    }
}