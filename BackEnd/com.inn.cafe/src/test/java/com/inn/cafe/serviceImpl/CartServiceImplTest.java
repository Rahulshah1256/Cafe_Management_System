package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.*;
import com.inn.cafe.dao.*;
import com.inn.cafe.dto.AddToCartRequest;
import com.inn.cafe.dto.ApplyCouponRequest;
import com.inn.cafe.dto.CheckoutRequest;
import com.inn.cafe.exception.ResourceNotFoundException;
import com.inn.cafe.exception.UnauthorizedException;
import com.inn.cafe.exception.ValidationException;
import com.inn.cafe.wrapper.CartWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock private CartDao cartDao;
    @Mock private CartItemDao cartItemDao;
    @Mock private productDao productDao;
    @Mock private UserDao userDao;
    @Mock private AddressDao addressDao;
    @Mock private CouponDao couponDao;
    @Mock private BillDao billDao;
    @Mock private JwtFilter jwtFilter;
    @Mock private com.inn.cafe.service.RazorpayService razorpayService;
    @Mock private com.inn.cafe.service.NotificationService notificationService;

    @InjectMocks
    private CartServiceImpl cartService;

    private User user;
    private Cart cart;
    private Category category;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setEmail("user@cafe.com");
        user.setName("Test User");

        cart = new Cart();
        cart.setId(100);
        cart.setUser(user);

        category = new Category();
        category.setId(1);
        category.setName("Pizza");

        lenient().when(jwtFilter.getCurrentUsername()).thenReturn("user@cafe.com");
        lenient().when(userDao.findByEmail("user@cafe.com")).thenReturn(user);
        lenient().when(cartDao.findByUserId(1)).thenReturn(Optional.of(cart));
        lenient().when(cartDao.findById(100)).thenReturn(Optional.of(cart));
        lenient().when(cartDao.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Product product(int id, int price, String status) {
        Product p = new Product();
        p.setId(id);
        p.setName("Margherita Pizza");
        p.setPrice(price);
        p.setstatus(status);
        p.setCategory(category);
        p.setIsVeg(true);
        return p;
    }

    private CartItem item(int id, Product p, int qty) {
        CartItem ci = new CartItem();
        ci.setId(id);
        ci.setCart(cart);
        ci.setProduct(p);
        ci.setQuantity(qty);
        return ci;
    }

    @Test
    void addItem_shouldCreateNewCartItem_whenProductNotAlreadyInCart() {
        Product p = product(10, 250, "true");
        when(productDao.findById(10)).thenReturn(Optional.of(p));
        when(cartItemDao.findByCartIdAndProductId(100, 10)).thenReturn(Optional.empty());
        when(cartItemDao.findByCartId(100)).thenReturn(Collections.singletonList(item(1, p, 2)));

        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(10);
        request.setQuantity(2);

        ResponseEntity<CartWrapper> response = cartService.addItem(request);

        verify(cartItemDao).save(any(CartItem.class));
        assertEquals(1, response.getBody().getItems().size());
        assertEquals(500.0, response.getBody().getSubtotal());
    }

    @Test
    void addItem_shouldIncrementQuantity_whenProductAlreadyInCart() {
        Product p = product(10, 250, "true");
        CartItem existing = item(1, p, 1);
        when(productDao.findById(10)).thenReturn(Optional.of(p));
        when(cartItemDao.findByCartIdAndProductId(100, 10)).thenReturn(Optional.of(existing));
        when(cartItemDao.findByCartId(100)).thenReturn(Collections.singletonList(existing));

        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(10);
        request.setQuantity(2);

        cartService.addItem(request);

        assertEquals(3, existing.getQuantity());
        verify(cartItemDao).save(existing);
    }

    @Test
    void addItem_shouldThrowValidation_whenProductInactive() {
        Product p = product(10, 250, "false");
        when(productDao.findById(10)).thenReturn(Optional.of(p));

        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(10);
        request.setQuantity(1);

        assertThrows(ValidationException.class, () -> cartService.addItem(request));
    }

    @Test
    void removeItem_shouldThrowUnauthorized_whenItemBelongsToAnotherCart() {
        Cart otherCart = new Cart();
        otherCart.setId(999);
        CartItem foreignItem = new CartItem();
        foreignItem.setId(5);
        foreignItem.setCart(otherCart);
        when(cartItemDao.findById(5)).thenReturn(Optional.of(foreignItem));

        assertThrows(UnauthorizedException.class, () -> cartService.removeItem(5));
    }

    @Test
    void getCart_shouldApplyFreeDelivery_whenSubtotalMeetsThreshold() {
        Product p = product(10, 600, "true");
        when(cartItemDao.findByCartId(100)).thenReturn(Collections.singletonList(item(1, p, 1)));

        CartWrapper wrapper = cartService.getCart().getBody();

        assertEquals(0.0, wrapper.getDeliveryCharge());
        assertEquals(600.0, wrapper.getSubtotal());
    }

    @Test
    void getCart_shouldChargeDelivery_whenBelowFreeThreshold() {
        Product p = product(10, 200, "true");
        when(cartItemDao.findByCartId(100)).thenReturn(Collections.singletonList(item(1, p, 1)));

        CartWrapper wrapper = cartService.getCart().getBody();

        assertEquals(40.0, wrapper.getDeliveryCharge());
    }

    @Test
    void applyCoupon_shouldApplyPercentageDiscountCappedAtMax() {
        Product p = product(10, 1000, "true");
        when(cartItemDao.findByCartId(100)).thenReturn(Collections.singletonList(item(1, p, 1)));

        Coupon coupon = new Coupon();
        coupon.setCode("SAVE20");
        coupon.setDiscountType("PERCENTAGE");
        coupon.setDiscountValue(20.0);
        coupon.setMaxDiscountAmount(100.0);
        coupon.setMinOrderAmount(0.0);
        coupon.setActive(true);
        when(couponDao.findByCodeIgnoreCase("SAVE20")).thenReturn(Optional.of(coupon));

        ApplyCouponRequest request = new ApplyCouponRequest();
        request.setCode("SAVE20");

        CartWrapper wrapper = cartService.applyCoupon(request).getBody();

        // 20% of 1000 = 200, capped at 100
        assertEquals(100.0, wrapper.getDiscount());
        assertEquals("SAVE20", wrapper.getAppliedCouponCode());
    }

    @Test
    void applyCoupon_shouldThrowValidation_whenExpired() {
        Product p = product(10, 1000, "true");
        when(cartItemDao.findByCartId(100)).thenReturn(Collections.singletonList(item(1, p, 1)));

        Coupon coupon = new Coupon();
        coupon.setCode("OLD10");
        coupon.setDiscountType("FLAT");
        coupon.setDiscountValue(10.0);
        coupon.setActive(true);
        coupon.setExpiryDate(LocalDate.now().minusDays(1));
        when(couponDao.findByCodeIgnoreCase("OLD10")).thenReturn(Optional.of(coupon));

        ApplyCouponRequest request = new ApplyCouponRequest();
        request.setCode("OLD10");

        assertThrows(ValidationException.class, () -> cartService.applyCoupon(request));
    }

    @Test
    void applyCoupon_shouldThrowValidation_whenMinOrderNotMet() {
        Product p = product(10, 100, "true");
        when(cartItemDao.findByCartId(100)).thenReturn(Collections.singletonList(item(1, p, 1)));

        Coupon coupon = new Coupon();
        coupon.setCode("BIG500");
        coupon.setDiscountType("FLAT");
        coupon.setDiscountValue(50.0);
        coupon.setActive(true);
        coupon.setMinOrderAmount(500.0);
        when(couponDao.findByCodeIgnoreCase("BIG500")).thenReturn(Optional.of(coupon));

        ApplyCouponRequest request = new ApplyCouponRequest();
        request.setCode("BIG500");

        assertThrows(ValidationException.class, () -> cartService.applyCoupon(request));
    }

    @Test
    void checkout_shouldThrowValidation_whenCartEmpty() {
        when(cartItemDao.findByCartId(100)).thenReturn(Collections.emptyList());

        CheckoutRequest request = new CheckoutRequest();
        request.setAddressId(1);
        request.setPaymentMethod("COD");

        assertThrows(ValidationException.class, () -> cartService.checkout(request));
    }

    @Test
    void checkout_shouldCreateBillAndClearCart_whenValid() {
        Product p = product(10, 250, "true");
        CartItem ci = item(1, p, 2);
        when(cartItemDao.findByCartId(100)).thenReturn(Collections.singletonList(ci));

        Address address = new Address();
        address.setId(5);
        address.setUser(user);
        address.setContactNumber("9000000000");
        when(addressDao.findById(5)).thenReturn(Optional.of(address));
        when(billDao.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));

        CheckoutRequest request = new CheckoutRequest();
        request.setAddressId(5);
        request.setPaymentMethod("COD");

        ResponseEntity<String> response = cartService.checkout(request);

        verify(billDao).save(any(Bill.class));
        verify(cartItemDao).deleteByCartId(100);
        assertTrue(response.getBody().contains("uuid"));
    }

    @Test
    void checkout_shouldThrowUnauthorized_whenAddressBelongsToAnotherUser() {
        Product p = product(10, 250, "true");
        when(cartItemDao.findByCartId(100)).thenReturn(Collections.singletonList(item(1, p, 1)));

        User otherUser = new User();
        otherUser.setId(2);
        Address address = new Address();
        address.setId(5);
        address.setUser(otherUser);
        when(addressDao.findById(5)).thenReturn(Optional.of(address));

        CheckoutRequest request = new CheckoutRequest();
        request.setAddressId(5);
        request.setPaymentMethod("COD");

        assertThrows(UnauthorizedException.class, () -> cartService.checkout(request));
    }

    @Test
    void checkout_shouldCreateRazorpayOrder_andStayPending_forOnlinePayment() {
        Product p = product(10, 250, "true");
        when(cartItemDao.findByCartId(100)).thenReturn(Collections.singletonList(item(1, p, 2)));

        Address address = new Address();
        address.setId(5);
        address.setUser(user);
        address.setContactNumber("9000000000");
        when(addressDao.findById(5)).thenReturn(Optional.of(address));
        when(billDao.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));
        when(razorpayService.createOrder(anyDouble(), anyString()))
                .thenReturn(new com.inn.cafe.wrapper.RazorpayOrderResult("order_ABC123", "rzp_test_key", 55500L, "INR"));

        CheckoutRequest request = new CheckoutRequest();
        request.setAddressId(5);
        request.setPaymentMethod("UPI");

        ResponseEntity<String> response = cartService.checkout(request);

        assertTrue(response.getBody().contains("\"paymentStatus\":\"PENDING\""));
        assertTrue(response.getBody().contains("order_ABC123"));
        assertTrue(response.getBody().contains("rzp_test_key"));
    }

    @Test
    void checkout_shouldAwardLoyaltyPoints_onCheckout() {
        Product p = product(10, 250, "true");
        when(cartItemDao.findByCartId(100)).thenReturn(Collections.singletonList(item(1, p, 2)));

        Address address = new Address();
        address.setId(5);
        address.setUser(user);
        address.setContactNumber("9000000000");
        when(addressDao.findById(5)).thenReturn(Optional.of(address));
        when(billDao.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));

        CheckoutRequest request = new CheckoutRequest();
        request.setAddressId(5);
        request.setPaymentMethod("COD");

        // subtotal 500 (free delivery) + packing 20 + platform 10 + tax 25 = total 555
        ResponseEntity<String> response = cartService.checkout(request);

        assertTrue(response.getBody().contains("\"loyaltyPointsEarned\":55"));
        assertEquals(55, user.getLoyaltyPoints());
        verify(userDao).save(user);
    }

    @Test
    void checkout_shouldRedeemLoyaltyPoints_whenRequestedAndAvailable() {
        user.setLoyaltyPoints(100);
        Product p = product(10, 250, "true");
        when(cartItemDao.findByCartId(100)).thenReturn(Collections.singletonList(item(1, p, 2)));

        Address address = new Address();
        address.setId(5);
        address.setUser(user);
        address.setContactNumber("9000000000");
        when(addressDao.findById(5)).thenReturn(Optional.of(address));
        when(billDao.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));

        CheckoutRequest request = new CheckoutRequest();
        request.setAddressId(5);
        request.setPaymentMethod("COD");
        request.setUseLoyaltyPoints(true);

        // total 555, 100 points redeemed (Rs.100 off) -> final total 455; earns floor(555*0.1)=55
        // on pre-redemption total; final balance = 100 - 100 + 55 = 55
        ResponseEntity<String> response = cartService.checkout(request);

        assertTrue(response.getBody().contains("\"total\":455"));
        assertTrue(response.getBody().contains("\"loyaltyPointsRedeemed\":100"));
        assertEquals(55, user.getLoyaltyPoints());
    }
}
