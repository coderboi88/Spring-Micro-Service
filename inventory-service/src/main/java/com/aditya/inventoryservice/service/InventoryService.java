package com.aditya.inventoryservice.service;

import com.aditya.inventoryservice.dto.InventoryResponse;
import com.aditya.inventoryservice.model.Inventory;
import com.aditya.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public Inventory findInventoryBySkuCode(String skuCode) {
        return inventoryRepository.findInventoryBySkuCode(skuCode).orElse(null);
     }

     @Transactional(readOnly = true)
     @SneakyThrows
    public List<InventoryResponse> getStockStatus(List<String> skuCodes) {
//        log.info("Start Thread Sleep");
//        Thread.sleep(10000);  // It has been done for testing Resilience
//        log.info("Stop Thread Sleep");

        List<Inventory> inventories = inventoryRepository.findBySkuCodeIn(skuCodes);

        return inventories.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    private InventoryResponse mapToResponseDto(Inventory inventory) {
        InventoryResponse inventoryResponse = new InventoryResponse();
        inventoryResponse.setSkuCode(inventory.getSkuCode());
        inventoryResponse.setInStock(inventory.getQuantity()>0);

        return inventoryResponse;
    }
}
