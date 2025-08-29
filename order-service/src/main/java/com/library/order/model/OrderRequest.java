package com.library.order.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record OrderRequest(
        @NotBlank String bookId,
        @NotBlank String userId,
        @Positive Double amount
) {
}
