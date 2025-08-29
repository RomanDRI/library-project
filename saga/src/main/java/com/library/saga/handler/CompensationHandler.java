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
public class CompensationHandler {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final SagaStateRepository sagaStateRepository;

    public Mono<Void> compensate(String orderId, String errorMessage) {
        return sagaStateRepository.findByOrderId(orderId).flatMap(sagaState -> {
            SagaState compensationState = sagaState.withStatus(SagaStatus.COMPENSATED);
            compensationState.setErrorMessage(errorMessage);
            return sagaStateRepository.save(sagaState);
        }).then(Mono.fromRunnable(() -> {
            OrderEvent orderEvent = new OrderEvent(
                    "comp_" + UUID.randomUUID(),
                    orderId,
                    null,
                    null,
                    OrderEvent.OrderStatus.COMPENSATION_REQUESTED,
                    null,
                    Instant.now()
            );
            kafkaTemplate.send("compensation-commands", orderId, orderEvent);
        }));
    }

}
