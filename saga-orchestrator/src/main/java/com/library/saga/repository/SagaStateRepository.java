package com.library.saga.repository;

import com.library.saga.model.SagaState;
import com.library.saga.model.SagaStatus;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface SagaStateRepository extends R2dbcRepository<SagaState, String> {
    Mono<SagaState> findByOrderId(String orderId);
    Flux<SagaState> findBySagaStatus(SagaStatus sagaStatus);
}
