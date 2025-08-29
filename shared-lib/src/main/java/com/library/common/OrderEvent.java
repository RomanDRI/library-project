package com.library.common;

import java.time.Instant;
import java.util.UUID;

public record OrderEvent(
        String eventId,
        String orderId,
        String bookId,
        String userId,
        OrderStatus status,
        Double amount,
        Instant timestamp
) {
    public enum OrderStatus {
        ORDER_CREATED,
        BOOKING_RESERVED,
        BOOKING_FAILED,
        PAYMENT_PROCESSED,
        PAYMENT_FAILED,
        ORDER_COMPLETED,
        ORDER_CANCELLED,
        BOOKING_REQUESTED,
        PAYMENT_REQUESTED,
        COMPENSATION_REQUESTED
    }

    public static OrderEvent create(String orderId, String bookId, String userId,
                                    OrderStatus status, Double amount) {
        return new OrderEvent(
                "evt_" + UUID.randomUUID(),
                orderId,
                bookId,
                userId,
                status,
                amount,
                Instant.now()
        );
    }

    public OrderEvent withStatus(OrderStatus newStatus) {
        return new OrderEvent(
                "evt_" + UUID.randomUUID(),
                this.orderId,
                this.bookId,
                this.userId,
                newStatus,
                this.amount,
                Instant.now()
        );
    }
}
