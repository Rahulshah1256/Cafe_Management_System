package com.inn.cafe.serviceImpl;

import com.google.gson.Gson;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.*;
import com.inn.cafe.constents.CafeConstants;
import com.inn.cafe.dao.*;
import com.inn.cafe.dto.AddToCartRequest;
import com.inn.cafe.dto.ApplyCouponRequest;
import com.inn.cafe.dto.CheckoutRequest;
import com.inn.cafe.dto.UpdateCartItemRequest;
import com.inn.cafe.exception.ResourceNotFoundException;
import com.inn.cafe.exception.UnauthorizedException;
import com.inn.cafe.exception.ValidationException;
import com.inn.cafe.service.CartService;
import com.inn.cafe.utils.CafeUtils;
import com.inn.cafe.wrapper.CartItemWrapper;
import com.inn.cafe.wrapper.CartWrapper;
import com.inn.cafe.wrapper.RazorpayOrderResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    CartDao cartDao;

    @Autowired
    CartItemDao cartItemDao;

    @Autowired
    productDao productDao;

    @Autowired
    UserDao userDao;

    @Autowired
    AddressDao addressDao;

    @Autowired
    CouponDao couponDao;

    @Autowired
    BillDao billDao;

    @Autowired
    com.inn.cafe.service.RazorpayService razorpayService;

    @Autowired
    com.inn.cafe.service.NotificationService notificationService;

    @Autowired
    JwtFilter jwtFilter;

    @Override
    public ResponseEntity<CartWrapper> getCart() {
        Cart cart = getOrCreateCart();
        return new ResponseEntity<>(buildWrapper(cart), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CartWrapper> addItem(AddToCartRequest request) {
        Cart cart = getOrCreateCart();
        Product product = productDao.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));
        if (!"true".equalsIgnoreCase(product.getstatus())) {
            throw new ValidationException("Product is currently unavailable: " + product.getName());
        }

        CartItem item = cartItemDao.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);
        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(request.getQuantity());
        } else {
            item.setQuantity(item.getQuantity() + request.getQuantity());
        }
        cartItemDao.save(item);
        log.info("Added product {} (qty {}) to cart for user {}", product.getName(), request.getQuantity(), jwtFilter.getCurrentUsername());
        return new ResponseEntity<>(buildWrapper(reload(cart)), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CartWrapper> updateItem(Integer itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart();
        CartItem item = getOwnedItem(cart, itemId);
        item.setQuantity(request.getQuantity());
        cartItemDao.save(item);
        return new ResponseEntity<>(buildWrapper(reload(cart)), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CartWrapper> removeItem(Integer itemId) {
        Cart cart = getOrCreateCart();
        CartItem item = getOwnedItem(cart, itemId);
        cartItemDao.delete(item);
        return new ResponseEntity<>(buildWrapper(reload(cart)), HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<CartWrapper> clearCart() {
        Cart cart = getOrCreateCart();
        cartItemDao.deleteByCartId(cart.getId());
        cart.setAppliedCouponCode(null);
        cartDao.save(cart);
        return new ResponseEntity<>(buildWrapper(reload(cart)), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CartWrapper> applyCoupon(ApplyCouponRequest request) {
        Cart cart = getOrCreateCart();
        List<CartItem> items = cartItemDao.findByCartId(cart.getId());
        if (items.isEmpty()) {
            throw new ValidationException("Cannot apply a coupon to an empty cart");
        }
        double subtotal = computeSubtotal(items);

        Coupon coupon = couponDao.findByCodeIgnoreCase(request.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid coupon code: " + request.getCode()));
        validateCoupon(coupon, subtotal);

        cart.setAppliedCouponCode(coupon.getCode());
        cartDao.save(cart);
        return new ResponseEntity<>(buildWrapper(reload(cart)), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CartWrapper> removeCoupon() {
        Cart cart = getOrCreateCart();
        cart.setAppliedCouponCode(null);
        cartDao.save(cart);
        return new ResponseEntity<>(buildWrapper(reload(cart)), HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<String> checkout(CheckoutRequest request) {
        User user = currentUser();
        Cart cart = getOrCreateCart();
        List<CartItem> items = cartItemDao.findByCartId(cart.getId());
        if (items.isEmpty()) {
            throw new ValidationException("Cannot checkout with an empty cart");
        }

        Address address = addressDao.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + request.getAddressId()));
        if (!address.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("This address does not belong to you");
        }

        PriceBreakdown breakdown = computeBreakdown(items, cart.getAppliedCouponCode());

        int pointsEarned = (int) Math.floor(breakdown.total * CafeConstants.LOYALTY_EARN_RATE);
        int pointsRedeemed = 0;
        double loyaltyDiscount = 0.0;
        int availablePoints = user.getLoyaltyPoints() == null ? 0 : user.getLoyaltyPoints();
        if (Boolean.TRUE.equals(request.getUseLoyaltyPoints()) && availablePoints > 0) {
            double maxRedeemableValue = Math.min(availablePoints * CafeConstants.LOYALTY_POINT_VALUE, breakdown.total);
            pointsRedeemed = (int) Math.floor(maxRedeemableValue / CafeConstants.LOYALTY_POINT_VALUE);
            loyaltyDiscount = pointsRedeemed * CafeConstants.LOYALTY_POINT_VALUE;
            breakdown.total = Math.round((breakdown.total - loyaltyDiscount) * 100) / 100.0;
        }

        Bill bill = new Bill();
        bill.setUuid(CafeUtils.getUUID());
        bill.setName(user.getName());
        bill.setEmail(user.getEmail());
        bill.setContactNumber(address.getContactNumber());
        bill.setDeliveryAddress(formatDeliveryAddress(address));
        bill.setPaymentMethod(request.getPaymentMethod());
        bill.setTotal((int) Math.round(breakdown.total));
        bill.setProductDetails(new Gson().toJson(buildInvoiceLines(items, breakdown, loyaltyDiscount)));
        bill.setCreatedBy(jwtFilter.getCurrentUsername());
        bill.setOrderStatus(CafeConstants.ORDER_STATUS_PLACED);
        bill.setLoyaltyPointsEarned(pointsEarned);
        bill.setLoyaltyPointsRedeemed(pointsRedeemed);

        String paymentMessage;
        String razorpayOrderIdForResponse = null;
        String razorpayKeyIdForResponse = null;
        long razorpayAmountForResponse = 0;
        if (CafeConstants.isCashPaymentMethod(request.getPaymentMethod())) {
            bill.setPaymentStatus(CafeConstants.PAYMENT_STATUS_PENDING);
            paymentMessage = "Order placed. Pay cash on delivery.";
        } else {
            // Real-time gateway integration: create a genuine Razorpay order up-front. The
            // frontend opens the Razorpay Checkout widget against this order (which itself
            // supports UPI, Cards, Net Banking and Wallets), then calls /payment/verify with
            // the signed payment confirmation once the customer completes payment.
            RazorpayOrderResult order = razorpayService.createOrder(breakdown.total, bill.getUuid());
            bill.setRazorpayOrderId(order.getOrderId());
            bill.setPaymentStatus(CafeConstants.PAYMENT_STATUS_PENDING);
            paymentMessage = "Order placed. Complete payment to confirm your order.";
            razorpayOrderIdForResponse = order.getOrderId();
            razorpayKeyIdForResponse = order.getKeyId();
            razorpayAmountForResponse = order.getAmountInPaise();
        }
        billDao.save(bill);

        user.setLoyaltyPoints(availablePoints - pointsRedeemed + pointsEarned);
        userDao.save(user);

        if (cart.getAppliedCouponCode() != null) {
            couponDao.findByCodeIgnoreCase(cart.getAppliedCouponCode()).ifPresent(c -> {
                c.setUsedCount(c.getUsedCount() == null ? 1 : c.getUsedCount() + 1);
                couponDao.save(c);
            });
        }

        cartItemDao.deleteByCartId(cart.getId());
        cart.setAppliedCouponCode(null);
        cartDao.save(cart);

        log.info("Checkout complete for user {}: bill uuid={}, total={}, paymentStatus={}",
                user.getEmail(), bill.getUuid(), bill.getTotal(), bill.getPaymentStatus());
        notificationService.notify(user.getEmail(), "Order Placed",
                "Your order " + bill.getUuid() + " for Rs." + bill.getTotal() + " has been placed. " + paymentMessage,
                "ORDER_STATUS", bill.getId());
        String json = "{\"uuid\":\"" + bill.getUuid() + "\",\"total\":" + bill.getTotal()
                + ",\"paymentStatus\":\"" + bill.getPaymentStatus() + "\""
                + ",\"orderStatus\":\"" + bill.getOrderStatus() + "\""
                + (bill.getTransactionId() != null ? ",\"transactionId\":\"" + bill.getTransactionId() + "\"" : "")
                + ",\"loyaltyPointsEarned\":" + pointsEarned
                + ",\"loyaltyPointsRedeemed\":" + pointsRedeemed
                + ",\"loyaltyPointsBalance\":" + user.getLoyaltyPoints()
                + (razorpayOrderIdForResponse != null
                        ? ",\"razorpayOrderId\":\"" + razorpayOrderIdForResponse + "\""
                                + ",\"razorpayKeyId\":\"" + razorpayKeyIdForResponse + "\""
                                + ",\"razorpayAmount\":" + razorpayAmountForResponse
                                + ",\"razorpayCurrency\":\"INR\""
                        : "")
                + ",\"message\":\"" + paymentMessage + "\"}";
        return new ResponseEntity<>(json, HttpStatus.OK);
    }

    // ---------- helpers ----------

    private Cart getOrCreateCart() {
        User user = currentUser();
        return cartDao.findByUserId(user.getId()).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(user);
            return cartDao.save(cart);
        });
    }

    private Cart reload(Cart cart) {
        return cartDao.findById(cart.getId()).orElse(cart);
    }

    private CartItem getOwnedItem(Cart cart, Integer itemId) {
        CartItem item = cartItemDao.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new UnauthorizedException("This cart item does not belong to you");
        }
        return item;
    }

    private User currentUser() {
        User user = userDao.findByEmail(jwtFilter.getCurrentUsername());
        if (user == null) {
            throw new ResourceNotFoundException("Current user not found");
        }
        return user;
    }

    private double computeSubtotal(List<CartItem> items) {
        return items.stream()
                .mapToDouble(i -> i.getProduct().getPrice() * i.getQuantity())
                .sum();
    }

    private void validateCoupon(Coupon coupon, double subtotal) {
        if (!Boolean.TRUE.equals(coupon.getActive())) {
            throw new ValidationException("This coupon is no longer active");
        }
        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDate.now())) {
            throw new ValidationException("This coupon has expired");
        }
        if (coupon.getUsageLimit() != null && coupon.getUsedCount() != null
                && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new ValidationException("This coupon has reached its usage limit");
        }
        if (coupon.getMinOrderAmount() != null && subtotal < coupon.getMinOrderAmount()) {
            throw new ValidationException("Minimum order amount of " + coupon.getMinOrderAmount() + " required for this coupon");
        }
    }

    private double computeDiscount(Coupon coupon, double subtotal) {
        if (coupon == null) {
            return 0.0;
        }
        double discount;
        if ("PERCENTAGE".equalsIgnoreCase(coupon.getDiscountType())) {
            discount = subtotal * (coupon.getDiscountValue() / 100.0);
            if (coupon.getMaxDiscountAmount() != null) {
                discount = Math.min(discount, coupon.getMaxDiscountAmount());
            }
        } else {
            discount = coupon.getDiscountValue();
        }
        return Math.min(discount, subtotal);
    }

    private PriceBreakdown computeBreakdown(List<CartItem> items, String couponCode) {
        double subtotal = computeSubtotal(items);
        Coupon coupon = null;
        double discount = 0.0;
        if (couponCode != null) {
            coupon = couponDao.findByCodeIgnoreCase(couponCode).orElse(null);
            if (coupon != null) {
                try {
                    validateCoupon(coupon, subtotal);
                    discount = computeDiscount(coupon, subtotal);
                } catch (ValidationException ex) {
                    // Coupon became invalid (expired/limit reached) between apply and checkout time;
                    // drop it silently rather than blocking checkout.
                    coupon = null;
                }
            }
        }
        double deliveryCharge = subtotal >= CafeConstants.FREE_DELIVERY_THRESHOLD ? 0.0 : CafeConstants.DELIVERY_CHARGE;
        double packingCharge = CafeConstants.PACKING_CHARGE;
        double platformFee = CafeConstants.PLATFORM_FEE;
        double tax = Math.round((subtotal - discount) * CafeConstants.TAX_RATE * 100) / 100.0;
        double total = (subtotal - discount) + deliveryCharge + packingCharge + platformFee + tax;

        PriceBreakdown breakdown = new PriceBreakdown();
        breakdown.subtotal = subtotal;
        breakdown.discount = discount;
        breakdown.deliveryCharge = deliveryCharge;
        breakdown.packingCharge = packingCharge;
        breakdown.platformFee = platformFee;
        breakdown.tax = tax;
        breakdown.total = Math.round(total * 100) / 100.0;
        breakdown.appliedCouponCode = coupon != null ? coupon.getCode() : null;
        return breakdown;
    }

    private CartWrapper buildWrapper(Cart cart) {
        List<CartItem> items = cartItemDao.findByCartId(cart.getId());
        PriceBreakdown breakdown = computeBreakdown(items, cart.getAppliedCouponCode());

        List<CartItemWrapper> itemWrappers = items.stream().map(i -> new CartItemWrapper(
                i.getId(),
                i.getProduct().getId(),
                i.getProduct().getName(),
                i.getProduct().getImageUrl(),
                i.getProduct().getIsVeg(),
                i.getProduct().getPrice(),
                i.getQuantity(),
                i.getProduct().getPrice() * i.getQuantity(),
                "true".equalsIgnoreCase(i.getProduct().getstatus())
        )).toList();

        int itemCount = items.stream().mapToInt(CartItem::getQuantity).sum();

        return new CartWrapper(itemWrappers, itemCount, breakdown.subtotal, breakdown.appliedCouponCode,
                breakdown.discount, breakdown.deliveryCharge, breakdown.packingCharge, breakdown.platformFee,
                breakdown.tax, breakdown.total);
    }

    private List<Map<String, Object>> buildInvoiceLines(List<CartItem> items, PriceBreakdown breakdown, double loyaltyDiscount) {
        List<Map<String, Object>> lines = items.stream().map(i -> {
            Map<String, Object> line = new LinkedHashMap<>();
            line.put("name", i.getProduct().getName());
            line.put("category", i.getProduct().getCategory().getName());
            line.put("quantity", String.valueOf(i.getQuantity()));
            line.put("price", (double) i.getProduct().getPrice());
            line.put("total", (double) (i.getProduct().getPrice() * i.getQuantity()));
            // Recommendations module: retained so past orders can be matched back to a live
            // Product row (charge lines below intentionally omit this field).
            line.put("productId", i.getProduct().getId());
            return line;
        }).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));

        if (breakdown.discount > 0) {
            lines.add(chargeLine("Discount" + (breakdown.appliedCouponCode != null ? " (" + breakdown.appliedCouponCode + ")" : ""), -breakdown.discount));
        }
        lines.add(chargeLine("Delivery Charge", breakdown.deliveryCharge));
        lines.add(chargeLine("Packing Charge", breakdown.packingCharge));
        lines.add(chargeLine("Platform Fee", breakdown.platformFee));
        lines.add(chargeLine("Tax", breakdown.tax));
        if (loyaltyDiscount > 0) {
            lines.add(chargeLine("Loyalty Points Redeemed", -loyaltyDiscount));
        }
        return lines;
    }

    // Delivery module: snapshot the address at checkout time as a single formatted string
    // (rather than relying on a live FK to Address, which the customer could edit/delete
    // after the order is placed).
    private String formatDeliveryAddress(Address address) {
        StringBuilder sb = new StringBuilder();
        sb.append(address.getAddressLine1());
        if (address.getAddressLine2() != null && !address.getAddressLine2().isBlank()) {
            sb.append(", ").append(address.getAddressLine2());
        }
        sb.append(", ").append(address.getCity()).append(", ").append(address.getState())
                .append(" - ").append(address.getPincode());
        if (address.getLandmark() != null && !address.getLandmark().isBlank()) {
            sb.append(" (Landmark: ").append(address.getLandmark()).append(")");
        }
        return sb.toString();
    }

    private Map<String, Object> chargeLine(String label, double amount) {
        Map<String, Object> line = new LinkedHashMap<>();
        line.put("name", label);
        line.put("category", "Charges");
        line.put("quantity", "1");
        line.put("price", amount);
        line.put("total", amount);
        return line;
    }

    private static class PriceBreakdown {
        double subtotal;
        double discount;
        double deliveryCharge;
        double packingCharge;
        double platformFee;
        double tax;
        double total;
        String appliedCouponCode;
    }
}
