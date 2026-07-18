package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Notification;
import com.inn.cafe.dao.NotificationDao;
import com.inn.cafe.exception.ResourceNotFoundException;
import com.inn.cafe.exception.UnauthorizedException;
import com.inn.cafe.utils.EmailUtil;
import com.inn.cafe.wrapper.NotificationWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock private NotificationDao notificationDao;
    @Mock private JwtFilter jwtFilter;
    @Mock private EmailUtil emailUtil;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification notification(String recipient, boolean read) {
        Notification n = new Notification();
        n.setId(1);
        n.setRecipientUsername(recipient);
        n.setTitle("Order Placed");
        n.setMessage("Your order has been placed");
        n.setType("ORDER_STATUS");
        n.setIsRead(read);
        return n;
    }

    @Test
    void getMyNotifications_shouldReturnOwnNotifications() {
        when(jwtFilter.getCurrentUsername()).thenReturn("user@cafe.com");
        when(notificationDao.findByRecipientUsernameOrderByCreatedAtDesc("user@cafe.com"))
                .thenReturn(List.of(notification("user@cafe.com", false)));

        ResponseEntity<List<NotificationWrapper>> response = notificationService.getMyNotifications();

        assertEquals(1, response.getBody().size());
    }

    @Test
    void getUnreadCount_shouldReturnCount() {
        when(jwtFilter.getCurrentUsername()).thenReturn("user@cafe.com");
        when(notificationDao.countByRecipientUsernameAndIsReadFalse("user@cafe.com")).thenReturn(3L);

        ResponseEntity<java.util.Map<String, Long>> response = notificationService.getUnreadCount();

        assertEquals(3L, response.getBody().get("unreadCount"));
    }

    @Test
    void markAsRead_shouldSucceed_whenOwner() {
        Notification n = notification("user@cafe.com", false);
        when(notificationDao.findById(1)).thenReturn(Optional.of(n));
        when(jwtFilter.getCurrentUsername()).thenReturn("user@cafe.com");
        when(notificationDao.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<NotificationWrapper> response = notificationService.markAsRead(1);

        assertTrue(response.getBody().getIsRead());
    }

    @Test
    void markAsRead_shouldThrowUnauthorized_whenNotOwner() {
        Notification n = notification("other@cafe.com", false);
        when(notificationDao.findById(1)).thenReturn(Optional.of(n));
        when(jwtFilter.getCurrentUsername()).thenReturn("user@cafe.com");

        assertThrows(UnauthorizedException.class, () -> notificationService.markAsRead(1));
    }

    @Test
    void markAsRead_shouldThrowNotFound_whenMissing() {
        when(notificationDao.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.markAsRead(99));
    }

    @Test
    void notify_shouldPersistNotification_andEmailBestEffort() {
        notificationService.notify("user@cafe.com", "Title", "Message", "ORDER_STATUS", 5);

        verify(notificationDao, times(1)).save(any(Notification.class));
        verify(emailUtil, times(1)).SendSimpleMessage(
                org.mockito.ArgumentMatchers.eq("user@cafe.com"),
                org.mockito.ArgumentMatchers.eq("Title"),
                org.mockito.ArgumentMatchers.eq("Message"),
                org.mockito.ArgumentMatchers.isNull());
    }

    @Test
    void notify_shouldNotThrow_whenEmailFails() {
        doThrow(new RuntimeException("SMTP down")).when(emailUtil)
                .SendSimpleMessage(any(), any(), any(), any());

        assertDoesNotThrow(() ->
                notificationService.notify("user@cafe.com", "Title", "Message", "ORDER_STATUS", 5));
        verify(notificationDao, times(1)).save(any(Notification.class));
    }
}
