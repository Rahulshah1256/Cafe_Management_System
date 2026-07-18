package com.inn.cafe.service;

import com.inn.cafe.POJO.Bill;
import com.inn.cafe.dto.OrderStatusUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface BillService {
    ResponseEntity<String> generateReport(Map<String, Object> requestMap);
    ResponseEntity<List<Bill>> getBills();

    ResponseEntity<Page<Bill>> getBillsPaged(int page, int size, String sortBy, String direction);

    ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap);

    ResponseEntity<String> delete(Integer id);

    ResponseEntity<Bill> updateOrderStatus(Integer id, OrderStatusUpdateRequest request);

    ResponseEntity<Bill> cancelOrder(Integer id);
}
