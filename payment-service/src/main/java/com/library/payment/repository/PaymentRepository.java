package com.library.payment.repository;

import com.library.payment.model.Payment;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface PaymentRepository extends R2dbcRepository<Payment, String> {
    Mono<Payment> findByOrderId(String orderId);
}
