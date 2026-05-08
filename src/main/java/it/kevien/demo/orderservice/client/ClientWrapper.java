package it.kevien.demo.orderservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import it.kevien.demo.orderservice.client.dto.InventoryAdjustmentRequest;
import it.kevien.demo.orderservice.client.dto.InventoryResponse;
import it.kevien.demo.orderservice.client.dto.ProductResponse;
import it.kevien.demo.orderservice.exception.ServiceUnavailableException;
import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ClientWrapper {
    private final InventoryClient inventoryClient;
    private final ProductClient productClient;

    @CircuitBreaker(name = "product-service", fallbackMethod = "productFallback")
    @Retry(name = "product-service")
    public ProductResponse getProduct(Long productId) {
        return productClient.getProduct(productId);
    }

    @CircuitBreaker(name = "inventory-service", fallbackMethod = "inventoryFallback")
    @Retry(name = "inventory-service")
    public InventoryResponse getInventory(Long productId) {
        return inventoryClient.findByProductId(productId);
    }

    @CircuitBreaker(name = "inventory-service", fallbackMethod = "inventoryFallback")
    @Retry(name = "inventory-service")
    public void decreaseInventory(Long productId, InventoryAdjustmentRequest inventoryAdjustmentRequest) {
        inventoryClient.decreaseInventory(productId, inventoryAdjustmentRequest);
    }

    @CircuitBreaker(name = "inventory-service", fallbackMethod = "inventoryFallback")
    @Retry(name = "inventory-service")
    public void increaseInventory(Long productId, InventoryAdjustmentRequest inventoryAdjustmentRequest) {
        inventoryClient.increaseInventory(productId, inventoryAdjustmentRequest);
    }

    private ProductResponse productFallback(Long productId, Throwable t) {
        throw new ServiceUnavailableException("product-service not available: " + t.getMessage());
    }

    private InventoryResponse inventoryFallback(Long productId, Throwable t) {
        throw new ServiceUnavailableException("inventory-service not available: " + t.getMessage());
    }

    private void inventoryFallback(Long productId, InventoryAdjustmentRequest inventoryAdjustmentRequest, Throwable t) {
        throw new ServiceUnavailableException("inventory-service not available: " + t.getMessage());
    }
}
