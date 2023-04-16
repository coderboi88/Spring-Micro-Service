package com.aditya.inventoryservice.api;

import com.aditya.inventoryservice.dto.InventoryResponse;
import com.aditya.inventoryservice.model.Inventory;
import com.aditya.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryApi {

    private final InventoryService inventoryService;

    @GetMapping("/{sku-code}")
    public Inventory findInventorybySkuCode(@PathVariable("sku-code") String skuCode) {
        return inventoryService.findInventoryBySkuCode(skuCode);
    }

    @GetMapping
    public List<InventoryResponse> getStockStatus(@RequestParam List<String> skuCodes) {
        return inventoryService.getStockStatus(skuCodes);
    }
}
