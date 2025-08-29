package com.library.order.service;

import com.library.common.OrderEvent;
import com.library.common.OrderStatus;
import com.library.order.model.Order;
import com.library.order.model.OrderRequest;
import com.library.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public Mono<Order> createOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setBookedId(orderRequest.bookId());
        order.setUserId(orderRequest.userId());
        order.setAmount(orderRequest.amount());
        order.setStatus(OrderStatus.ORDER_CREATED);
        order.setCreatedAt(Instant.now());

        return orderRepository.save(order).doOnSuccess(saveOrder -> {
            OrderEvent event = OrderEvent.create(order.getId(), order.getBookedId(), order.getUserId(), OrderEvent.OrderStatus.ORDER_CREATED, order.getAmount());
            kafkaTemplate.send("order-events", event);
        });
    }

    @KafkaListener(topics = "saga-events", groupId = "order-service")
    public void handleSagaEvent(OrderEvent event) {
        if (event.status() == OrderEvent.OrderStatus.ORDER_COMPLETED) {
            orderRepository.updateOrderStatus(event.orderId(), OrderStatus.ORDER_COMPLETED).subscribe();
        } else if (event.status() == OrderEvent.OrderStatus.ORDER_CANCELLED) {
            orderRepository.updateOrderStatus(event.orderId(), OrderStatus.ORDER_CANCELLED).subscribe();
        }
    }
}