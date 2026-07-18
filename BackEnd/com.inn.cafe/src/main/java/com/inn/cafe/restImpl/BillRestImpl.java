package com.inn.cafe.restImpl;

import com.inn.cafe.POJO.Bill;
import com.inn.cafe.rest.BillRest;
import com.inn.cafe.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class BillRestImpl implements BillRest {
    @Autowired
    BillService billService;

    @Override
    public ResponseEntity<String> generateReport(Map<String, Object> requestMap) {
        return billService.generateReport(requestMap);
    }

    @Override
    public ResponseEntity<List<Bill>> getBills() {
        return billService.getBills();
    }

    @Override
    public ResponseEntity<org.springframework.data.domain.Page<Bill>> getBillsPaged(int page, int size, String sortBy, String direction) {
        return billService.getBillsPaged(page, size, sortBy, direction);
    }

    @Override
    public ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap) {
        return billService.getPdf(requestMap);
    }


    @Override
    public ResponseEntity<String> deleteBill(Integer id) {
        return billService.delete(id);
    }

    @Override
    public ResponseEntity<Bill> updateOrderStatus(Integer id, com.inn.cafe.dto.OrderStatusUpdateRequest request) {
        return billService.updateOrderStatus(id, request);
    }

    @Override
    public ResponseEntity<Bill> cancelOrder(Integer id) {
        return billService.cancelOrder(id);
    }

}

