package com.library.order.repository;

import com.library.common.OrderStatus;
import com.library.order.model.Order;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface OrderRepository extends R2dbcRepository<Order, String> {
    Mono<Integer> updateOrderStatus(@Param("id") String id, @Param("status") OrderStatus status);
}
