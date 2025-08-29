package com.library.payment.service;

import com.library.common.OrderEvent;
import com.library.payment.model.Payment;
import com.library.payment.model.PaymentStatus;
import com.library.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    private Mono<Payment> processPayment(OrderEvent orderEvent) {
        boolean paymentSuccess = Math.random() > 0.2;

        Payment payment = new Payment();
        payment.setId(UUID.randomUUID().toString());
        payment.setOrderId(orderEvent.orderId());
        payment.setAmount(orderEvent.amount());
        payment.setStatus(paymentSuccess ? PaymentStatus.PROCESSED : PaymentStatus.FAILED);

        return paymentRepository.save(payment).doOnSuccess(savedPayment -> {
            OrderEvent event = orderEvent.withStatus(paymentSuccess ? OrderEvent.OrderStatus.PAYMENT_PROCESSED : OrderEvent.OrderStatus.BOOKING_FAILED);
            kafkaTemplate.send("saga-events", event);
        });
    }

    @KafkaListener(topics = "saga-event", groupId = "payment-service")
    private void handle(OrderEvent orderEvent) {
        if (orderEvent.status() == OrderEvent.OrderStatus.PAYMENT_PROCESSED) {
            processPayment(orderEvent).subscribe();
        } else if (orderEvent.status() == OrderEvent.OrderStatus.PAYMENT_FAILED) {
            refundPayment(orderEvent.orderId()).subscribe();
        }
    }

    private Mono<Void> refundPayment(String orderId) {
        return paymentRepository.findByOrderId(orderId).flatMap(payment -> {
            payment.setStatus(PaymentStatus.REFUNDED);
            return paymentRepository.save(payment);
        }).then();
    }
}
