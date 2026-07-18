package com.inn.cafe.constents;

public class CafeConstants {
    public static final String SOMETHING_WENT_WRONG = "Something Went Wrong.";

    public static final String INVALID_INFO = "Invalid Data.";

    public static final String UNAUTHORIZED_ACCESS = "unauthorized access.";
    public static final String INVALID_DATA = "Invalid Data.";
    // Fallback only; the actual path is externalized via cafe.pdf.store-location in
    // application.properties (env var PDF_STORE_LOCATION) so it works across environments.
    public static final String STORE_LOCATION = "E:\\Cafe System\\com.inn.cafe\\CafeStoredFiles";

    // Cart & Checkout pricing rules (Domino's-style flat fees; could later be made
    // configurable per-restaurant/admin-managed, but flat constants are sufficient for now).
    public static final double DELIVERY_CHARGE = 40.0;
    public static final double FREE_DELIVERY_THRESHOLD = 500.0;
    public static final double PACKING_CHARGE = 20.0;
    public static final double PLATFORM_FEE = 10.0;
    public static final double TAX_RATE = 0.05;

    // Payment status values (Bill.paymentStatus)
    public static final String PAYMENT_STATUS_PENDING = "PENDING";
    public static final String PAYMENT_STATUS_SUCCESS = "SUCCESS";
    public static final String PAYMENT_STATUS_FAILED = "FAILED";
    public static final String PAYMENT_STATUS_REFUNDED = "REFUNDED";

    // Order status values (Bill.orderStatus) - order tracking timeline
    public static final String ORDER_STATUS_PLACED = "PLACED";
    public static final String ORDER_STATUS_ACCEPTED = "ACCEPTED";
    public static final String ORDER_STATUS_PREPARING = "PREPARING";
    public static final String ORDER_STATUS_OUT_FOR_DELIVERY = "OUT_FOR_DELIVERY";
    public static final String ORDER_STATUS_DELIVERED = "DELIVERED";
    public static final String ORDER_STATUS_CANCELLED = "CANCELLED";

    public static final java.util.List<String> VALID_ORDER_STATUSES = java.util.List.of(
            ORDER_STATUS_PLACED, ORDER_STATUS_ACCEPTED, ORDER_STATUS_PREPARING,
            ORDER_STATUS_OUT_FOR_DELIVERY, ORDER_STATUS_DELIVERED, ORDER_STATUS_CANCELLED);

    // Ordered timeline (excludes CANCELLED, which is a terminal side-branch rather than a step).
    public static final java.util.List<String> ORDER_STATUS_TIMELINE = java.util.List.of(
            ORDER_STATUS_PLACED, ORDER_STATUS_ACCEPTED, ORDER_STATUS_PREPARING,
            ORDER_STATUS_OUT_FOR_DELIVERY, ORDER_STATUS_DELIVERED);

    // Customers may only self-cancel while the order hasn't started being prepared yet.
    public static final java.util.Set<String> CUSTOMER_CANCELLABLE_STATUSES = java.util.Set.of(
            ORDER_STATUS_PLACED, ORDER_STATUS_ACCEPTED);

    // Kitchen Dashboard: orders still "in flight" that kitchen/delivery staff need to act on.
    public static final java.util.List<String> KITCHEN_QUEUE_STATUSES = java.util.List.of(
            ORDER_STATUS_PLACED, ORDER_STATUS_ACCEPTED, ORDER_STATUS_PREPARING,
            ORDER_STATUS_OUT_FOR_DELIVERY);

    // Delivery module: a dedicated role (distinct from "admin"/"user") for rider accounts,
    // created only by an admin via DeliveryService.registerPartner (no public self-signup).
    public static final String ROLE_DELIVERY_PARTNER = "delivery";

    public static final String DELIVERY_STATUS_AVAILABLE = "AVAILABLE";
    public static final String DELIVERY_STATUS_BUSY = "BUSY";
    public static final String DELIVERY_STATUS_OFFLINE = "OFFLINE";

    public static final java.util.List<String> VALID_DELIVERY_AVAILABILITY_STATUSES = java.util.List.of(
            DELIVERY_STATUS_AVAILABLE, DELIVERY_STATUS_BUSY, DELIVERY_STATUS_OFFLINE);

    // Invoice line items with this category represent fees/discounts (delivery charge,
    // tax, loyalty redemption, etc.) rather than an actual purchased product - excluded
    // when computing top-selling products for Sales Analytics.
    public static final String INVOICE_LINE_CHARGES_CATEGORY = "Charges";

    // Loyalty program: customers earn floor(orderTotal * LOYALTY_EARN_RATE) points per order
    // (computed on the pre-redemption total, so redeeming points doesn't reduce future earning),
    // and may redeem points at checkout at a fixed rate of LOYALTY_POINT_VALUE currency per point.
    public static final double LOYALTY_EARN_RATE = 0.1;
    public static final double LOYALTY_POINT_VALUE = 1.0;

    private static final java.util.Set<String> CASH_PAYMENT_METHODS = java.util.Set.of(
            "COD", "CASH", "CASH ON DELIVERY");

    // Cash-based payment methods are collected on delivery, so they never go through
    // the (mock) online payment gateway at checkout time.
    public static boolean isCashPaymentMethod(String paymentMethod) {
        return paymentMethod != null && CASH_PAYMENT_METHODS.contains(paymentMethod.trim().toUpperCase());
    }

}
