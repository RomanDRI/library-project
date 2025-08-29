package com.library.order.model;

import com.library.common.OrderStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Table(name = "orders")
public class Order {
    @Id
    private String id;
    private String bookedId;
    private String userId;
    private OrderStatus status;
    private Double amount;
    private Instant createdAt;
}
