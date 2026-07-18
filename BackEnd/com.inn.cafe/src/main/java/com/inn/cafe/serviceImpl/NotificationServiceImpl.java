package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Notification;
import com.inn.cafe.dao.NotificationDao;
import com.inn.cafe.exception.ResourceNotFoundException;
import com.inn.cafe.exception.UnauthorizedException;
import com.inn.cafe.service.NotificationService;
import com.inn.cafe.utils.EmailUtil;
import com.inn.cafe.wrapper.NotificationWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    NotificationDao notificationDao;

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    EmailUtil emailUtil;

    @Override
    public ResponseEntity<List<NotificationWrapper>> getMyNotifications() {
        List<NotificationWrapper> list = notificationDao
                .findByRecipientUsernameOrderByCreatedAtDesc(jwtFilter.getCurrentUsername())
                .stream().map(this::toWrapper).toList();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        long count = notificationDao.countByRecipientUsernameAndIsReadFalse(jwtFilter.getCurrentUsername());
        return new ResponseEntity<>(Map.of("unreadCount", count), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<NotificationWrapper> markAsRead(Integer id) {
        Notification notification = notificationDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        if (!notification.getRecipientUsername().equalsIgnoreCase(jwtFilter.getCurrentUsername())) {
            throw new UnauthorizedException("This notification does not belong to you");
        }
        notification.setIsRead(true);
        notificationDao.save(notification);
        return new ResponseEntity<>(toWrapper(notification), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> markAllAsRead() {
        List<Notification> unread = notificationDao
                .findByRecipientUsernameAndIsReadFalse(jwtFilter.getCurrentUsername());
        unread.forEach(n -> n.setIsRead(true));
        notificationDao.saveAll(unread);
        return new ResponseEntity<>("{\"message\":\"All notifications marked as read\"}", HttpStatus.OK);
    }

    @Override
    public void notify(String recipientUsername, String title, String message, String type, Integer relatedBillId) {
        Notification notification = new Notification();
        notification.setRecipientUsername(recipientUsername);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRelatedBillId(relatedBillId);
        notificationDao.save(notification);
        log.info("Notification created for {}: {}", recipientUsername, title);

        // Best-effort email; a misconfigured/unavailable mail server must never break the
        // calling business flow (checkout, order-status update, etc.), so failures are logged only.
        try {
            emailUtil.SendSimpleMessage(recipientUsername, title, message, null);
        } catch (Exception ex) {
            log.warn("Failed to email notification to {}: {}", recipientUsername, ex.getMessage());
        }
    }

    private NotificationWrapper toWrapper(Notification n) {
        return new NotificationWrapper(n.getId(), n.getTitle(), n.getMessage(), n.getType(),
                n.getRelatedBillId(), n.getIsRead(), n.getCreatedAt());
    }
}
