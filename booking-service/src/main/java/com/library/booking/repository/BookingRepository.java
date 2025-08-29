package com.library.booking.repository;

import com.library.booking.model.Booking;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface BookingRepository extends R2dbcRepository<Booking, String> {
    Mono<Booking> findByOrderId(String orderId);
}
