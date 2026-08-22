package com.scrapDetection.service.impl;

import com.scrapDetection.dto.material.MaterialRequestDTO;
import com.scrapDetection.dto.material.MaterialResponseDTO;
import com.scrapDetection.entity.Account;
import com.scrapDetection.entity.Material;
import com.scrapDetection.entity.ScrapYard;
import com.scrapDetection.exception.ResourceAlreadyExistsException;
import com.scrapDetection.mapper.MaterialMapper;
import com.scrapDetection.repository.MaterialRepository;
import com.scrapDetection.repository.TransactionRepository;
import com.scrapDetection.service.AccountService;
import com.scrapDetection.util.Normalize;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaterialServiceImplTest {

    @Mock
    private MaterialRepository materialRepository;
    @Mock
    private MaterialMapper materialMapper;
    @Mock
    private AccountService accountService;
    @Mock
    private TransactionRepository transactionRepository;
    @Spy
    private Normalize normalize = new Normalize();

    @InjectMocks
    private MaterialServiceImpl materialService;

    @Test
    void createMaterial_duplicateNameInSameYardIncludingInactive_throwsConflict() {
        ScrapYard yard = ScrapYard.builder().yardId(10L).build();
        Account owner = Account.builder().scrapYard(yard).build();
        MaterialRequestDTO request = request("  Sắt   vụn ");
        Material inactiveDuplicate = Material.builder()
                .materialId(30L)
                .itemName(" SẮT  VỤN ")
                .status("INACTIVE")
                .scrapYard(yard)
                .build();

        Material mapped = Material.builder().itemName("  Sắt   vụn ").build();

        when(accountService.getCurrentUser()).thenReturn(owner);
        when(materialMapper.toEntity(request)).thenReturn(mapped);
        when(materialRepository.existsByScrapYardYardIdAndItemNameIgnoreCase(10L, "sắt   vụn")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class,
                () -> materialService.createMaterial(request));

        verify(materialRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createMaterial_uniqueName_normalizesBeforeSaving() {
        ScrapYard yard = ScrapYard.builder().yardId(10L).build();
        Account owner = Account.builder().scrapYard(yard).build();
        MaterialRequestDTO request = request("  Giấy   carton ");
        Material mapped = Material.builder().itemName("Giấy carton").build();
        Material saved = Material.builder().materialId(20L).itemName("Giấy carton").scrapYard(yard).build();
        MaterialResponseDTO response = MaterialResponseDTO.builder().materialId(20L).itemName("Giấy carton").build();

        when(accountService.getCurrentUser()).thenReturn(owner);
        when(materialMapper.toEntity(request)).thenReturn(mapped);
        when(materialRepository.existsByScrapYardYardIdAndItemNameIgnoreCase(10L, "giấy carton")).thenReturn(false);
        when(materialRepository.save(mapped)).thenReturn(saved);
        when(materialMapper.toResponseDTO(saved)).thenReturn(response);

        MaterialResponseDTO result = materialService.createMaterial(request);

        assertEquals(20L, result.getMaterialId());
        verify(materialRepository).save(mapped);
    }

    @Test
    void updateMaterial_renameToAnotherMaterialInSameYard_throwsConflict() {
        ScrapYard yard = ScrapYard.builder().yardId(10L).build();
        Account owner = Account.builder().scrapYard(yard).build();
        Material existing = Material.builder().materialId(20L).itemName("Đồng").scrapYard(yard).build();
        MaterialRequestDTO request = request(" nhôm ");
        Material duplicate = Material.builder()
                .materialId(21L)
                .itemName("Nhôm")
                .scrapYard(yard)
                .build();

        when(materialRepository.findById(20L)).thenReturn(Optional.of(existing));
        when(accountService.getCurrentUser()).thenReturn(owner);
        when(materialRepository.existsByScrapYardYardIdAndItemNameIgnoreCase(10L, "nhôm")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class,
                () -> materialService.updateMaterial(20L, request));

        verify(materialRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private MaterialRequestDTO request(String itemName) {
        return MaterialRequestDTO.builder()
                .itemName(itemName)
                .itemPrice(10_000D)
                .unit("kg")
                .status("ACTIVE")
                .icon("icon_metal")
                .build();
    }
}
