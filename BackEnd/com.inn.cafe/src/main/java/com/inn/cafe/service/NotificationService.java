package com.inn.cafe.service;

import com.inn.cafe.wrapper.NotificationWrapper;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface NotificationService {

    ResponseEntity<List<NotificationWrapper>> getMyNotifications();

    ResponseEntity<Map<String, Long>> getUnreadCount();

    ResponseEntity<NotificationWrapper> markAsRead(Integer id);

    ResponseEntity<String> markAllAsRead();

    // Internal API used by other services (Cart/Bill/Payment) to raise a notification for a
    // user; not exposed over REST. Also best-effort emails the user via EmailUtil.
    void notify(String recipientUsername, String title, String message, String type, Integer relatedBillId);
}
