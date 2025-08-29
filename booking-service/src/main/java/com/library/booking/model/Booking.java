package com.library.booking.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Table("Bookings")
public class Booking {
    @Id
    private String id;

    @Column("order_id")
    private String orderId;

    @Column("book_id")
    private String bookId;

    @Column("booking_status")
    private BookingStatus bookingStatus;

    @Column("created_at")
    private Instant cratedAt;
}
