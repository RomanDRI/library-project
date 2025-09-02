package com.library.saga.handler;

import com.library.common.OrderEvent;
import com.library.saga.model.SagaState;
import com.library.saga.model.SagaStatus;
import com.library.saga.repository.SagaStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentHandler {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final SagaStateRepository sagaStateRepository;

    public Mono<Void> processPayment(OrderEvent event) {
        return sagaStateRepository.findByOrderId(event.orderId()).flatMap(sagaState -> {
            SagaState state = sagaState.withStatus(SagaStatus.PAYMENT_IN_PROGRESS);
            return sagaStateRepository.save(state);
        }).then(Mono.fromRunnable(() -> {
            OrderEvent orderEvent = new OrderEvent(
                    "cmd_" + UUID.randomUUID(),
                    event.orderId(),
                    event.bookId(),
                    event.userId(),
                    OrderEvent.OrderStatus.PAYMENT_REQUESTED,
                    event.amount(),
                    Instant.now()
            );
            kafkaTemplate.send("payment-commands", event.orderId(), orderEvent);
        }));
    }

}
