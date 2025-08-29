package com.library.order.controller;

import com.library.order.model.Order;
import com.library.order.model.OrderRequest;
import com.library.order.repository.OrderRepository;
import com.library.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @PostMapping
    public ResponseEntity<Mono<Order>> createOrder(@RequestBody OrderRequest orderRequest) {
        return new ResponseEntity<>(orderService.createOrder(orderRequest), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mono<Order>> getOrder(@PathVariable String id) {
        return new ResponseEntity<>(orderRepository.findById(id), HttpStatus.OK);
    }
}
