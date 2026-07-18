package com.inn.cafe.POJO;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "notification")
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    // Username (email) of the recipient - mirrors Bill.createdBy's convention of storing the
    // username directly rather than a foreign key, keeping this module loosely coupled.
    @Column(name = "recipient_username", nullable = false)
    private String recipientUsername;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", nullable = false)
    private String message;

    // ORDER_STATUS, PAYMENT, OFFER - free-form type tag for frontend icon/grouping, not enforced.
    @Column(name = "type")
    private String type;

    // Nullable - only set for order/payment-related notifications, absent for e.g. offer broadcasts.
    @Column(name = "related_bill_id")
    private Integer relatedBillId;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = Boolean.FALSE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
