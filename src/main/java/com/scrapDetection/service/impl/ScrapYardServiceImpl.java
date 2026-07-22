package com.scrapDetection.service.impl;

import com.scrapDetection.dto.scrapyard.ScrapYardRequestDTO;
import com.scrapDetection.dto.scrapyard.ScrapYardResponseDTO;
import com.scrapDetection.dto.scrapyard.ScrapYardStatusRequestDTO;
import com.scrapDetection.dto.scrapyard.ScrapYardUpdateRequestDTO;
import com.scrapDetection.entity.Role;
import com.scrapDetection.entity.ScrapYard;
import com.scrapDetection.exception.ResourceAlreadyExistsException;
import com.scrapDetection.exception.ResourceNotFoundException;
import com.scrapDetection.mapper.ScrapYardMapper;
import com.scrapDetection.repository.ScrapYardRepository;
import com.scrapDetection.service.AccountService;
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
    private final AccountService accountService;

    @Override
    public ScrapYardResponseDTO createScrapYardRequest(ScrapYardRequestDTO requestDTO) {
        requestDTO.setPhoneNumbers(requestDTO.getPhoneNumbers().trim());
        if (scrapYardRepository.existsByYardName(requestDTO.getYardName())) {
            throw new ResourceAlreadyExistsException("Scrap Yard", "yardName", requestDTO.getYardName());
        }

        if (scrapYardRepository.existsByPhoneNumbers(requestDTO.getPhoneNumbers())) {
            throw new ResourceAlreadyExistsException("Scrap Yard", "phoneNumbers", requestDTO.getPhoneNumbers());
        }

        if(scrapYardRepository.existsByAddress(requestDTO.getAddress())) {
            throw new ResourceAlreadyExistsException("Scrap Yard", "address", requestDTO.getAddress());
        }

        if (scrapYardRepository.existsByPhoneNumbers(requestDTO.getYardOwnerPhoneNumber())) {
            throw new ResourceAlreadyExistsException("Account", "phoneNumbers", requestDTO.getPhoneNumbers());
        }

        ScrapYard scrapYard = scrapYardMapper.toEntity(requestDTO);

        if (scrapYard.getStatus() == null || scrapYard.getStatus().isBlank()) {
            scrapYard.setStatus("PENDING");
        }

        ScrapYard savedYard = scrapYardRepository.save(scrapYard);
        accountService.registerCustomer(scrapYardMapper.scrapYardToAccountRequest(requestDTO), scrapYard.getYardId());
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
    public Page<ScrapYardResponseDTO> getAllActiveScrapYards(Pageable pageable) {
        Page<ScrapYard> yardPage = scrapYardRepository.findByStatus("Active",pageable);
        return yardPage.map(scrapYardMapper::toResponseDTO);
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
    public ScrapYardResponseDTO updateScrapYard(Long yardId, ScrapYardUpdateRequestDTO requestDTO) {
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
    public ScrapYardResponseDTO updateScrapYardStatus(ScrapYardStatusRequestDTO requestDTO, Long id) {
        ScrapYard existingYard = scrapYardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scrap Yard", id));

        if(existingYard.getStatus().equals("PENDING") && requestDTO.getStatus().equals("ACTIVE")){
            accountService.changeRole(existingYard.getYardId(), Role.CUSTOMER, Role.YARD_OWNER);
        }

        existingYard.setStatus(requestDTO.getStatus());

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
    public ScrapYardResponseDTO getScrapYardByName(String yardName) {
        ScrapYard scrapYard = scrapYardRepository.findByYardName(yardName)
                .orElseThrow(() -> new ResourceNotFoundException("Scrap Yard", "yardName", yardName));

        return scrapYardMapper.toResponseDTO(scrapYard);
    }

    @Override
    public List<ScrapYardResponseDTO> searchScrapYardsByName(String yardName) {
        List<ScrapYard> yards = scrapYardRepository.findByYardNameContainingIgnoreCase(yardName);
        return scrapYardMapper.toResponseDTOList(yards);
    }
}