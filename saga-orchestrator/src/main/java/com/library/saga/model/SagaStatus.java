package com.library.saga.model;

public enum SagaStatus {
    STARTED,
    BOOKING_IN_PROGRESS,
    BOOKING_COMPLETED,
    PAYMENT_IN_PROGRESS,
    COMPLETED,
    COMPENSATING,
    COMPENSATED,
    FAILED
}
