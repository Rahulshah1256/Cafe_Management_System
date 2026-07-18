package com.inn.cafe.rest;

import com.inn.cafe.wrapper.NotificationWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/notification")
public interface NotificationRest {

    @GetMapping(path = "/get")
    ResponseEntity<List<NotificationWrapper>> getMyNotifications();

    @GetMapping(path = "/unreadCount")
    ResponseEntity<Map<String, Long>> getUnreadCount();

    @PutMapping(path = "/read/{id}")
    ResponseEntity<NotificationWrapper> markAsRead(@PathVariable Integer id);

    @PutMapping(path = "/readAll")
    ResponseEntity<String> markAllAsRead();
}
