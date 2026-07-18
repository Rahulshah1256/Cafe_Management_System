package com.inn.cafe.POJO;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@NamedQuery(name = "Bill.getAllBills" , query = "select b from Bill b order by b.id desc")
@NamedQuery(name = "Bill.getBillByUserName" , query = "select b from Bill b where b.createdBy=:username order by b.id desc")


@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "bill")
public class Bill implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "contactnumber")
    private String contactNumber;

    @Column(name = "paymentmethod")
    private String paymentMethod;

    @Column(name = "total")
    private Integer total;

    // Explicit JDBC JSON type mapping required for Postgres json columns (Hibernate 6 otherwise
    // sends the parameter as varchar, which Postgres rejects with a type-mismatch error).
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "productdetails" , columnDefinition = "json")
    private String productDetails;

    @Column(name = "createdby")
    private String createdBy;

    // Payment & order tracking (populated by the customer checkout flow via CartService;
    // remains null for legacy admin POS bills created directly through ManageOrder).
    @Column(name = "paymentstatus")
    private String paymentStatus;

    @Column(name = "transactionid")
    private String transactionId;

    @Column(name = "orderstatus")
    private String orderStatus;

    // Populated automatically on insert; older legacy rows (pre-dating this column) remain
    // null, which the tracking UI treats as "unknown date" rather than failing.
    @CreationTimestamp
    @Column(name = "createdat", updatable = false)
    private Instant createdAt;

    // Loyalty points awarded/spent on this order - recorded so cancelOrder() can reverse the
    // exact amounts on the user's balance (rather than recomputing, which could drift).
    @Column(name = "loyaltypointsearned")
    private Integer loyaltyPointsEarned;

    @Column(name = "loyaltypointsredeemed")
    private Integer loyaltyPointsRedeemed;

    // Delivery module: a formatted snapshot of the delivery address taken at checkout time
    // (rather than a live FK to Address, which the customer could edit/delete later - orders
    // must retain the exact address they were placed against).
    @Column(name = "deliveryaddress")
    private String deliveryAddress;

    // Email of the delivery partner (User.role = "delivery") currently assigned to this order;
    // null until an admin assigns one via DeliveryService.assignPartner().
    @Column(name = "assigneddeliverypartner")
    private String assignedDeliveryPartner;

    // Populated when the assigned delivery partner marks the order DELIVERED.
    @Column(name = "deliveredat")
    private Instant deliveredAt;

    // Razorpay order id for online payment methods - created up-front at checkout so the
    // frontend can open the Razorpay Checkout widget against it, then verified via
    // PaymentService.verifyPayment() once the customer completes payment.
    @Column(name = "razorpayorderid")
    private String razorpayOrderId;

}
