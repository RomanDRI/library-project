package com.library.saga.orchestrator;

import com.library.common.OrderEvent;
import com.library.saga.handler.BookingHandler;
import com.library.saga.handler.CompensationHandler;
import com.library.saga.handler.PaymentHandler;
import com.library.saga.model.SagaState;
import com.library.saga.model.SagaStatus;
import com.library.saga.repository.SagaStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class OrderSagaOrchestrator {

    private final SagaStateRepository sagaStateRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final BookingHandler bookingHandler;
    private final PaymentHandler paymentHandler;
    private final CompensationHandler compensationHandler;

    private Mono<Void> startSaga(OrderEvent orderEvent) {
        SagaState initialState = new SagaState(orderEvent.orderId(), SagaStatus.STARTED);

        return sagaStateRepository.save(initialState).then(bookingHandler.reserveBooking(orderEvent))
                .doOnError(error -> compensationHandler.compensate(orderEvent.orderId(), error.getMessage()));
    }

    private Mono<Void> handleBookingSuccess(OrderEvent orderEvent) {
        return sagaStateRepository.findByOrderId(orderEvent.orderId()).flatMap(state -> {
                    SagaState updatedState = state.withStatus(SagaStatus.BOOKING_COMPLETED);
                    return sagaStateRepository.save(updatedState);
                })
                .then(paymentHandler.processPayment(orderEvent))
                .doOnError(error -> compensationHandler.compensate(orderEvent.orderId(), "Payment initiation failed"));

    }

    private Mono<Void> handlerBookingFailure(OrderEvent orderEvent) {
        return compensationHandler.compensate(orderEvent.orderId(), "Booking failure: " + orderEvent);
    }

    private Mono<Void> handlerPaymentSuccess(OrderEvent orderEvent) {
        return sagaStateRepository.findByOrderId(orderEvent.orderId()).flatMap(state -> {
            SagaState completeState = state.withStatus(SagaStatus.COMPLETED);
            return sagaStateRepository.save(completeState);
        }).then(Mono.fromRunnable(() -> {
            OrderEvent completedEvent = orderEvent.withStatus(OrderEvent.OrderStatus.ORDER_COMPLETED);
            kafkaTemplate.send("saga-events", orderEvent.orderId(), completedEvent);
        }));
    }

    private Mono<Void> handlerPaymentFailure(OrderEvent orderEvent) {
        return compensationHandler.compensate(orderEvent.orderId(), "Payment failure: " + orderEvent);
    }

    @KafkaListener(topics = "order-events", groupId = "saga-orchestrator")
    public Mono<Void> handlerOrderEvent(OrderEvent orderEvent) {
        return switch (orderEvent.status()) {
            case ORDER_CREATED -> startSaga(orderEvent);
            case BOOKING_RESERVED -> handleBookingSuccess(orderEvent);
            case BOOKING_FAILED -> handlerBookingFailure(orderEvent);
            case PAYMENT_PROCESSED -> handlerPaymentSuccess(orderEvent);
            case PAYMENT_FAILED -> handlerPaymentFailure(orderEvent);
            default -> Mono.empty();
        };
    }

}