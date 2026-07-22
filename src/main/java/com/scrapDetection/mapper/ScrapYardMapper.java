package com.scrapDetection.mapper;

import com.scrapDetection.dto.account.CreateAccountRequestDTO;
import com.scrapDetection.dto.scrapyard.ScrapYardRequestDTO;
import com.scrapDetection.dto.scrapyard.ScrapYardResponseDTO;
import com.scrapDetection.dto.scrapyard.ScrapYardUpdateRequestDTO;
import com.scrapDetection.entity.ScrapYard;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScrapYardMapper {

    public ScrapYard toEntity(ScrapYardRequestDTO dto) {
        if (dto == null) return null;

        ScrapYard scrapYard = new ScrapYard();
        scrapYard.setYardName(dto.getYardName());
        scrapYard.setAddress(dto.getAddress());
        scrapYard.setPhoneNumbers(dto.getPhoneNumbers());
        return scrapYard;
    }

    public CreateAccountRequestDTO scrapYardToAccountRequest (ScrapYardRequestDTO dto) {
        CreateAccountRequestDTO accountRequest = new CreateAccountRequestDTO();
        accountRequest.setAccountName(dto.getDisplayName());
        accountRequest.setPhoneNumbers(dto.getYardOwnerPhoneNumber());
        accountRequest.setPassword(dto.getPassword());
        accountRequest.setEmail(dto.getEmail());
        return accountRequest;
    }

    public ScrapYardResponseDTO toResponseDTO(ScrapYard entity) {
        if (entity == null) return null;

        return ScrapYardResponseDTO.builder()
                .yardId(entity.getYardId())
                .yardName(entity.getYardName())
                .address(entity.getAddress())
                .phoneNumbers(entity.getPhoneNumbers())
                .openHour(entity.getOpenHour())
                .closeHour(entity.getCloseHour())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<ScrapYardResponseDTO> toResponseDTOList(List<ScrapYard> entities) {
        return entities.stream()
                .map(this ::toResponseDTO)
                .collect(Collectors.toList());
    }

    // For partial updates
    public void updateEntityFromDTO(ScrapYardUpdateRequestDTO dto, ScrapYard entity) {
        if (dto.getYardName() != null) {
            entity.setYardName(dto.getYardName());
        }
        if (dto.getAddress() != null) {
            entity.setAddress(dto.getAddress());
        }
        if (dto.getPhoneNumbers() != null) {
            entity.setPhoneNumbers(dto.getPhoneNumbers());
        }
        if(dto.getOpenHour() != null){
            entity.setOpenHour(dto.getOpenHour());
        }
        if(dto.getCloseHour() != null){
            entity.setCloseHour(dto.getCloseHour());
        }
    }
}
