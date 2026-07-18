package com.inn.cafe.dao;

import com.inn.cafe.POJO.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationDao extends JpaRepository<Notification, Integer> {
    List<Notification> findByRecipientUsernameOrderByCreatedAtDesc(String recipientUsername);

    long countByRecipientUsernameAndIsReadFalse(String recipientUsername);

    List<Notification> findByRecipientUsernameAndIsReadFalse(String recipientUsername);
}
