package it.kevien.demo.orderservice.model.mapper;

import it.kevien.demo.orderservice.model.Order;
import it.kevien.demo.orderservice.model.dto.OrderRequest;
import it.kevien.demo.orderservice.model.dto.OrderResponse;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    Order toEntity(OrderRequest orderRequest);
    OrderResponse toDto(Order order);
    List<OrderResponse> toDto(List<Order> orders);
}
