package com.inn.cafe.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationWrapper {
    private Integer id;
    private String title;
    private String message;
    private String type;
    private Integer relatedBillId;
    private Boolean isRead;
    private Instant createdAt;
}
