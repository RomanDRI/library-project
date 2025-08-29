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
public class BookingHandler {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final SagaStateRepository sagaStateRepository;

    public Mono<Void> reserveBooking(OrderEvent event) {
        return sagaStateRepository.findByOrderId(event.orderId())
                .flatMap(sagaState -> {
                    SagaState state = sagaState.withStatus(SagaStatus.BOOKING_IN_PROGRESS);
                    return sagaStateRepository.save(state);
                })
                .then(Mono.fromRunnable(() -> {
                    OrderEvent bookingEvent = new OrderEvent(
                            "cmd_" + UUID.randomUUID(),
                            event.orderId(),
                            event.bookId(),
                            event.userId(),
                            OrderEvent.OrderStatus.BOOKING_REQUESTED,
                            event.amount(),
                            Instant.now()
                    );
                    kafkaTemplate.send("booking-commands", event.orderId(), bookingEvent);
                }));
    }
}
