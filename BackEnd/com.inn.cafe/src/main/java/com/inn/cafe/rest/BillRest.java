package com.inn.cafe.rest;

import com.inn.cafe.POJO.Bill;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/bill")
public interface BillRest {
    @PostMapping(path = "/generateReport")
    public ResponseEntity<String> generateReport(@RequestBody Map<String, Object> requestMap);

    @GetMapping(path = "/getBills")
    public ResponseEntity<List<Bill>> getBills();

    @GetMapping(path = "/getBills/paged")
    public ResponseEntity<Page<Bill>> getBillsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction);

    @PostMapping(path = "/getPdf")
    public ResponseEntity<byte[]> getPdf(@RequestBody Map<String, Object> requestMap);

    @PostMapping(path = "/delete/{id}")
    public ResponseEntity<String> deleteBill(@PathVariable Integer id);

    @PutMapping(path = "/orderStatus/{id}")
    public ResponseEntity<Bill> updateOrderStatus(@PathVariable Integer id,
                                                   @jakarta.validation.Valid @RequestBody com.inn.cafe.dto.OrderStatusUpdateRequest request);

    @PutMapping(path = "/cancel/{id}")
    public ResponseEntity<Bill> cancelOrder(@PathVariable Integer id);

}

