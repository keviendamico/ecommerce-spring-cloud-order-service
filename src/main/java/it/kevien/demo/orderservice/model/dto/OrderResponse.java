package it.kevien.demo.orderservice.model.dto;

import it.kevien.demo.orderservice.model.OrderStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(Long id, Long productId, Integer quantity, BigDecimal totalPrice, OrderStatusEnum status, LocalDateTime createdAt) {

}
