package it.kevien.demo.orderservice.client;

import it.kevien.demo.orderservice.client.dto.ProductResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/products")
@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/{id}")
    ProductResponse getProduct(@PathVariable Long id);
}
