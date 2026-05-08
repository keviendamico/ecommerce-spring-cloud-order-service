package it.kevien.demo.orderservice.service;

import it.kevien.demo.orderservice.client.ClientWrapper;
import it.kevien.demo.orderservice.client.InventoryClient;
import it.kevien.demo.orderservice.client.ProductClient;
import it.kevien.demo.orderservice.client.dto.InventoryAdjustmentRequest;
import it.kevien.demo.orderservice.client.dto.InventoryResponse;
import it.kevien.demo.orderservice.client.dto.ProductResponse;
import it.kevien.demo.orderservice.exception.InventoryQuantityNotEnoughException;
import it.kevien.demo.orderservice.exception.OrderNotFoundException;
import it.kevien.demo.orderservice.model.Order;
import it.kevien.demo.orderservice.model.OrderStatusEnum;
import it.kevien.demo.orderservice.model.dto.OrderRequest;
import it.kevien.demo.orderservice.model.dto.OrderResponse;
import it.kevien.demo.orderservice.model.mapper.OrderMapper;
import it.kevien.demo.orderservice.repository.OrderRepository;
import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ClientWrapper clientWrapper;

    public OrderResponse getOrder(Long orderId) {
        Optional<Order> opt = orderRepository.findById(orderId);
        return opt.map(orderMapper::toDto).orElseThrow(() -> new OrderNotFoundException("Order with id '" + orderId +"' not found"));
    }

    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orderMapper.toDto(orders);
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest) {
        ProductResponse product = clientWrapper.getProduct(orderRequest.productId());
        InventoryResponse inventory = clientWrapper.getInventory(product.id());
        if (inventory.quantity() < orderRequest.quantity()) {
            throw new InventoryQuantityNotEnoughException("Inventory quantity less than product quantity. Product ID: " + product.id());
        }
        Order order = orderMapper.toEntity(orderRequest);
        order.setTotalPrice(BigDecimal.valueOf(orderRequest.quantity()).multiply(product.price()));
        order.setStatus(OrderStatusEnum.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        orderRepository.save(order);
        clientWrapper.decreaseInventory(orderRequest.productId(), new InventoryAdjustmentRequest(orderRequest.quantity()));
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderResponse updateOrder(Long orderId, OrderRequest orderRequest) {
        ProductResponse product = clientWrapper.getProduct(orderRequest.productId());
        InventoryResponse inventory = clientWrapper.getInventory(product.id());
        Optional<Order> opt = orderRepository.findById(orderId);
        if (opt.isPresent()) {
            Order order = opt.get();
            order.setProductId(orderRequest.productId());
            if (orderRequest.quantity() > order.getQuantity()) {
                Integer delta = orderRequest.quantity() - order.getQuantity();
                if (inventory.quantity() < delta) {
                    throw new InventoryQuantityNotEnoughException("Inventory quantity less than product quantity. Product ID: " + product.id());
                }
                clientWrapper.decreaseInventory(orderRequest.productId(), new InventoryAdjustmentRequest(delta));
            }
            if (orderRequest.quantity() < order.getQuantity()) {
                Integer delta = order.getQuantity() - orderRequest.quantity();
                clientWrapper.increaseInventory(orderRequest.productId(), new InventoryAdjustmentRequest(delta));
            }
            order.setQuantity(orderRequest.quantity());
            order.setTotalPrice(BigDecimal.valueOf(orderRequest.quantity()).multiply(product.price()));
            orderRepository.save(order);
            return orderMapper.toDto(order);
        }
        throw new OrderNotFoundException("Order with id '" + orderId +"' not found");
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        Optional<Order> opt = orderRepository.findById(orderId);
        if (opt.isPresent()) {
            Order order = opt.get();
            clientWrapper.increaseInventory(order.getProductId(), new InventoryAdjustmentRequest(order.getQuantity()));
            orderRepository.deleteById(orderId);
            return;
        }
        throw new OrderNotFoundException("Order with id '" + orderId + "' not found");
    }
}
