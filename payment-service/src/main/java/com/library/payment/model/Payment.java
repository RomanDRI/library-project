package com.library.payment.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode
@Table("payments")
public class Payment {
    @Id
    private String id;

    @Column("order_id")
    private String orderId;

    private Double amount;
    private PaymentStatus status;

    @Column("transaction_id")
    private String transactionId;

    @Column("created_at")
    private Instant createdAt;
}
