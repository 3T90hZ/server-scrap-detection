package com.scrapDetection.service.impl;

import com.scrapDetection.dto.scrapyard.ScrapYardRequestDTO;
import com.scrapDetection.dto.scrapyard.ScrapYardResponseDTO;
import com.scrapDetection.dto.scrapyard.ScrapYardStatusRequestDTO;
import com.scrapDetection.dto.scrapyard.ScrapYardUpdateRequestDTO;
import com.scrapDetection.entity.Account;
import com.scrapDetection.entity.Material;
import com.scrapDetection.entity.Role;
import com.scrapDetection.entity.ScrapYard;
import com.scrapDetection.exception.InvalidRequestException;
import com.scrapDetection.exception.ResourceAlreadyExistsException;
import com.scrapDetection.exception.ResourceNotFoundException;
import com.scrapDetection.mapper.ScrapYardMapper;
import com.scrapDetection.repository.AccountRepository;
import com.scrapDetection.repository.MaterialRepository;
import com.scrapDetection.repository.ScrapYardRepository;
import com.scrapDetection.service.AccountService;
import com.scrapDetection.service.ScrapYardService;
import com.scrapDetection.util.Normalize;
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
    private final AccountRepository accountRepository;
    private final MaterialRepository materialRepository;
    private final ScrapYardMapper scrapYardMapper;
    private final AccountService accountService;
    private final Normalize normalize;

    @Override
    public ScrapYardResponseDTO createScrapYardRequest(ScrapYardRequestDTO requestDTO) {
        requestDTO.setPhoneNumbers(normalize.normalizeEmailAndPhoneNumber(requestDTO.getPhoneNumbers()));
        requestDTO.setEmail(normalize.normalizeEmailAndPhoneNumber(requestDTO.getEmail()));

        Account account = accountRepository.findByPhoneNumbers(normalize.normalizeEmailAndPhoneNumber(requestDTO.getPhoneNumbers())).orElse(null);
        if (CheckYardNameDuplicate(requestDTO.getYardName())) {
            throw new ResourceAlreadyExistsException("Scrap Yard", "yardName", requestDTO.getYardName());
        }

        if (scrapYardRepository.existsByPhoneNumbers(requestDTO.getPhoneNumbers())) {
            throw new ResourceAlreadyExistsException("Scrap Yard", "phoneNumbers", requestDTO.getPhoneNumbers());
        }

        if(scrapYardRepository.existsByAddress(requestDTO.getAddress())) {
            throw new ResourceAlreadyExistsException("Scrap Yard", "address", requestDTO.getAddress());
        }

        ScrapYard scrapYard = scrapYardMapper.toEntity(requestDTO);

        if (scrapYard.getStatus() == null || scrapYard.getStatus().isBlank()) {
            scrapYard.setStatus("PENDING");
        }

        ScrapYard savedYard = scrapYardRepository.save(scrapYard);
        if(account != null) {
            if( account.getRole().equals(Role.CUSTOMER) && account.getScrapYard() == null) {
                account.setScrapYard(savedYard);
            }
            else {
                throw new InvalidRequestException("Already belong to a yard!");
            }
        }else {
            accountService.registerCustomer(scrapYardMapper.scrapYardToAccountRequest(requestDTO), scrapYard.getYardId());
        }
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
    public Page<ScrapYardResponseDTO> getScrapYardsByStatus(String status, Pageable pageable) {
        Page<ScrapYard> yards = scrapYardRepository.findByStatus(status,pageable);
        return yards.map(scrapYardMapper::toResponseDTO);
    }

    @Override
    public ScrapYardResponseDTO updateScrapYard(Long yardId, ScrapYardUpdateRequestDTO requestDTO) {
        checkYardOwnership(yardId);
        ScrapYard existingYard = scrapYardRepository.findById(yardId)
                .orElseThrow(() -> new ResourceNotFoundException("Scrap Yard", yardId));

        requestDTO.setPhoneNumbers(requestDTO.getPhoneNumbers().trim());

        if(scrapYardRepository.existsByAddress(requestDTO.getAddress()) && !existingYard.getAddress().equals(requestDTO.getAddress())) {
            throw new  ResourceAlreadyExistsException("Scrap Yard", "address", requestDTO.getAddress());
        }
        if(scrapYardRepository.existsByPhoneNumbers(requestDTO.getPhoneNumbers()) && !existingYard.getPhoneNumbers().equals(requestDTO.getPhoneNumbers())) {
            throw new ResourceAlreadyExistsException("Scrap Yard", "phoneNumbers", requestDTO.getPhoneNumbers());
        }
        if(CheckYardNameDuplicate(requestDTO.getYardName()) && !existingYard.getYardName().equals(requestDTO.getYardName())) {
            throw new ResourceAlreadyExistsException("Scrap Yard", "yardName", requestDTO.getYardName());
        }
        // Update entity from DTO
        scrapYardMapper.updateEntityFromDTO(requestDTO, existingYard);

        ScrapYard updatedYard = scrapYardRepository.save(existingYard);
        return scrapYardMapper.toResponseDTO(updatedYard);
    }

    @Override
    public ScrapYardResponseDTO updateScrapYardStatus(ScrapYardStatusRequestDTO requestDTO, Long id) {
        checkYardOwnership(id);
        ScrapYard existingYard = scrapYardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scrap Yard", id));

        if(existingYard.getStatus().equals("PENDING")
                && requestDTO.getStatus().equals("ACTIVE")
                && accountService.getCurrentUser().getRole() ==  Role.ADMIN) {
            accountService.changeRole(existingYard.getYardId(), Role.CUSTOMER, Role.YARD_OWNER);
        }

        existingYard.setStatus(requestDTO.getStatus());

        ScrapYard updatedYard = scrapYardRepository.save(existingYard);
        return scrapYardMapper.toResponseDTO(updatedYard);
    }

    @Override
    public void deleteScrapYard(Long yardId) {
        ScrapYard existingYard = scrapYardRepository.findById(yardId)
                .orElseThrow(() -> new ResourceNotFoundException("Scrap Yard", yardId));

        List<Material> materials = materialRepository.findByScrapYardYardId(yardId);
        accountRepository.findByScrapYardYardId(yardId).forEach(acc -> {
            acc.setScrapYard(null);
            acc.setRole(Role.CUSTOMER);
            accountRepository.save(acc);
        });
        if(!materials.isEmpty()) {
            materials.forEach(material -> {material.setStatus("INACTIVE"); materialRepository.save(material);});
            existingYard.setStatus("INACTIVE");
            scrapYardRepository.save(existingYard);
        }else {
            scrapYardRepository.deleteById(yardId);
        }
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

    private Boolean CheckYardNameDuplicate(String yardName) {
        String normalized = normalize.normalizeName(yardName);
        return scrapYardRepository.existsByYardNameIgnoreCase(normalized);
    }

    private void checkYardOwnership(Long yardId){
        if(accountService.getCurrentUser().getRole() == Role.YARD_OWNER
                && !accountService.getCurrentUser().getScrapYard().getYardId().equals(yardId)){
            throw new InvalidRequestException("No permission!");
        }
    }
}