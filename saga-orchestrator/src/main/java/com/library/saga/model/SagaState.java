package com.library.saga.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Table("saga_states")
public class SagaState {
    @Id
    private String id;

    @Column("order_id")
    private String orderId;

    @Column("saga_status")
    private SagaStatus sagaStatus;

    @Column("current_step")
    private String currentStep;

    @Column("compensation_data")
    private String compensationData;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;

    @Column("error_message")
    private String errorMessage;

    public SagaState(String orderId, SagaStatus sagaStatus) {
        this.id = "saga_" + UUID.randomUUID();
        this.orderId = orderId;
        this.sagaStatus = sagaStatus;
        this.currentStep = "INIT";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public SagaState withStatus(SagaStatus newStatus) {
        SagaState state = new SagaState();
        state.id = this.id;
        state.orderId = this.orderId;
        state.sagaStatus = newStatus;
        state.currentStep = this.currentStep;
        state.compensationData = this.compensationData;
        state.createdAt = this.createdAt;
        state.updatedAt = Instant.now();
        state.errorMessage = this.errorMessage;
        return state;
    }
}