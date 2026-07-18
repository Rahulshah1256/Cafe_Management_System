package com.inn.cafe.serviceImpl;

import com.inn.cafe.exception.ValidationException;
import com.inn.cafe.service.RazorpayService;
import com.inn.cafe.wrapper.RazorpayOrderResult;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RazorpayServiceImpl implements RazorpayService {

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    @Override
    public RazorpayOrderResult createOrder(double amountInRupees, String receipt) {
        long amountInPaise = Math.round(amountInRupees * 100);
        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", receipt);
            orderRequest.put("payment_capture", 1);
            Order order = client.orders.create(orderRequest);
            String orderId = order.get("id");
            log.info("Razorpay order created: {} for amount {} paise (receipt {})", orderId, amountInPaise, receipt);
            return new RazorpayOrderResult(orderId, keyId, amountInPaise, "INR");
        } catch (RazorpayException | org.json.JSONException e) {
            log.error("Razorpay order creation failed for receipt {}: {}", receipt, e.getMessage());
            throw new ValidationException(
                    "Payment gateway is not available right now (" + e.getMessage() + "). "
                            + "Please try Cash on Delivery or contact support.");
        }
    }

    @Override
    public boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);
            return Utils.verifyPaymentSignature(options, keySecret);
        } catch (RazorpayException | org.json.JSONException e) {
            log.error("Razorpay signature verification failed for order {}: {}", orderId, e.getMessage());
            return false;
        }
    }
}
