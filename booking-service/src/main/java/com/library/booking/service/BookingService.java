package com.library.booking.service;

import com.library.booking.model.Booking;
import com.library.booking.model.BookingStatus;
import com.library.booking.repository.BookingRepository;
import com.library.common.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final BookingRepository bookingRepository;

    private Mono<Booking> reserveBook(OrderEvent orderEvent) {

        Booking booking = new Booking();

        booking.setId(UUID.randomUUID().toString());
        booking.setOrderId(orderEvent.orderId());
        booking.setBookId(orderEvent.bookId());
        booking.setBookingStatus(BookingStatus.RESERVED);
        booking.setCratedAt(Instant.now());

        return bookingRepository.save(booking).doOnSuccess(saveBooking -> {
            OrderEvent successEvent = orderEvent.withStatus(OrderEvent.OrderStatus.BOOKING_RESERVED);
            kafkaTemplate.send("saga_events", successEvent);
        }).onErrorResume(e -> {
            OrderEvent errorEvent = orderEvent.withStatus(OrderEvent.OrderStatus.BOOKING_FAILED);
            kafkaTemplate.send("saga_events", errorEvent);
            return Mono.error(e);
        });
    }

    @KafkaListener(topics = "order_events", groupId = "booking_service")
    public void handleEventOrder(OrderEvent orderEvent) {
        if (orderEvent.status() == OrderEvent.OrderStatus.ORDER_CREATED) {
            reserveBook(orderEvent).subscribe();
        } else if (orderEvent.status() == OrderEvent.OrderStatus.ORDER_CANCELLED) {
            cancelBooking(orderEvent.orderId()).subscribe();
        }
    }

    @Transactional
    public Mono<Void> cancelBooking(String orderId) {
        return bookingRepository.findByOrderId(orderId)
                .flatMap(booking -> {
                    booking.setBookingStatus(BookingStatus.CANCELED);
                    return bookingRepository.save(booking);
                }).then();
    }

}
