package com.inn.cafe.restImpl;

import com.inn.cafe.rest.NotificationRest;
import com.inn.cafe.service.NotificationService;
import com.inn.cafe.wrapper.NotificationWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class NotificationRestImpl implements NotificationRest {

    @Autowired
    NotificationService notificationService;

    @Override
    public ResponseEntity<List<NotificationWrapper>> getMyNotifications() {
        return notificationService.getMyNotifications();
    }

    @Override
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        return notificationService.getUnreadCount();
    }

    @Override
    public ResponseEntity<NotificationWrapper> markAsRead(Integer id) {
        return notificationService.markAsRead(id);
    }

    @Override
    public ResponseEntity<String> markAllAsRead() {
        return notificationService.markAllAsRead();
    }
}
