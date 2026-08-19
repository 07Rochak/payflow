package com.rochak.payflow.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "Payment",
        description = """
                Internal JPA persistence model representing a PayFlow payment lifecycle.

                A Payment links the PayFlow payment record with its Razorpay order and payment
                identifiers. It also records verification timestamps and failure information.
                This is a persistence model and is not used directly as the public REST response.
                """
)public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Unique database identifier of the PayFlow payment.",
            example = "12",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @Column(nullable = false, unique = true)
    @Schema(
            description = "Unique Razorpay order identifier created for this PayFlow payment.",
            example = "order_RazorpayTest123"
    )
    private String razorpayOrderId;

    @Schema(
            description = "Razorpay payment identifier returned after successful Checkout.",
            example = "pay_RazorpayTest123"
    )
    private String razorpayPaymentId;

    @Column(nullable = false)
    @Schema(
            description = "Payment amount in INR.",
            example = "500.0"
    )
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(
            description = "Current lifecycle status of the PayFlow payment.",
            example = "PENDING"
    )
    private PaymentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @Schema(
            description = "User who initiated the payment.",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private User user;

    @Schema(
            description = "Timestamp at which the PayFlow payment record was created.",
            example = "2026-08-19T12:30:00"
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "Timestamp at which the Razorpay payment was successfully verified.",
            example = "2026-08-19T12:31:15"
    )
    private LocalDateTime verifiedAt;

    @Schema(
            description = "Timestamp of the most recent update to the payment record.",
            example = "2026-08-19T12:31:15"
    )
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    @Schema(
            description = """
                    Reason recorded when payment processing fails. NONE indicates that no payment failure has been recorded.
                    """,
            example = "NONE"
    )
    private PaymentFailureReason failureReason = PaymentFailureReason.NONE;

    @Column(nullable = false, unique = true)
    @Schema(
            description = "Unique PayFlow receipt identifier associated with the payment order.",
            example = "PAY_20260819_000012"
    )
    private String receiptId;
}
