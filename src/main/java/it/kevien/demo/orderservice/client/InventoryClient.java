package it.kevien.demo.orderservice.client;

import it.kevien.demo.orderservice.client.dto.InventoryAdjustmentRequest;
import it.kevien.demo.orderservice.client.dto.InventoryResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @GetMapping("/api/inventory/{productId}")
    InventoryResponse findByProductId(@PathVariable Long productId);

    @PatchMapping("/api/inventory/{productId}/decrease")
    InventoryResponse decreaseInventory(@PathVariable Long productId, @RequestBody InventoryAdjustmentRequest inventoryAdjustmentRequest);

    @PatchMapping("/api/inventory/{productId}/increase")
    InventoryResponse increaseInventory(@PathVariable Long productId, @RequestBody InventoryAdjustmentRequest inventoryAdjustmentRequest);
}
