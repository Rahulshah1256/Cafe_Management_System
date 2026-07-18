package com.inn.cafe.serviceImpl;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.inn.cafe.JWT.CustomerUserDetailsService;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Bill;
import com.inn.cafe.POJO.Category;
import com.inn.cafe.constents.CafeConstants;
import com.inn.cafe.dao.BillDao;
import com.inn.cafe.service.BillService;
import com.inn.cafe.utils.CafeUtils;
import com.inn.cafe.utils.EmailUtil;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
public class BillServiceImpl implements BillService {
    @Autowired
    BillDao billDao;

    @Autowired
    com.inn.cafe.dao.UserDao userDao;

    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    com.inn.cafe.JWT.jwtUtil jwtUtil;

    @Autowired
    JwtFilter jwtFilter;
    @Autowired
    CustomerUserDetailsService customerUserDetailsService;

    @Autowired
    EmailUtil emailUtil;

    @Autowired
    com.inn.cafe.service.NotificationService notificationService;

    // Externalized so PDF invoices work across environments (defaults to the OS temp dir
    // instead of a hardcoded Windows drive letter that may not exist).
    @org.springframework.beans.factory.annotation.Value("${cafe.pdf.store-location:${java.io.tmpdir}/cafe-bills}")
    private String storeLocation;

    @Override
    public ResponseEntity<String> generateReport(Map<String, Object> requestMap) {
        log.info("Insert generateReport");
        try {
            String filename;
            if (validateResquestMap(requestMap)) {
                if (requestMap.containsKey("isGenerate") && !(Boolean) requestMap.get("isGenerate")) {
                    filename = (String) requestMap.get("uuid");
                } else {
                    filename = CafeUtils.getUUID();
                    requestMap.put("uuid", filename);
                    insertBill(requestMap);
                }
                // print user data (name , email m contactNumber , ...)
                String data = "Name: " + requestMap.get("name") + "\n" + "Contact Number: " + requestMap.get("contactNumber") +
                        "\n" + "Email: " + requestMap.get("email") + "\n" + "Payment Method: " + requestMap.get("paymentMethod");
                Document document = new Document();
                new File(storeLocation).mkdirs();
                PdfWriter.getInstance(document, new FileOutputStream(storeLocation + File.separator + filename + ".pdf"));
                document.open();
                setRectaangleInPdf(document);

                // print pdf Header
                Paragraph chunk = new Paragraph("Cafe Management System", getFont("Header"));
                chunk.setAlignment(Element.ALIGN_CENTER);
                document.add(chunk);


                Paragraph paragraph = new Paragraph(data + "\n \n", getFont("Data"));
                document.add(paragraph);

                // Create table in pdf to print data
                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                addTableHeader(table);


                // Print table data
                JSONArray jsonArray = CafeUtils.getJsonArrayFromString((String) requestMap.get("productDetails"));
                for (int i = 0; i < jsonArray.length(); i++) {
                    addRows(table, CafeUtils.getMapFromJson(jsonArray.getString(i)));
                }

                document.add(table);

                // print pdf Footer
                Paragraph footer = new Paragraph("Total : " + requestMap.get("totalAmount") + "\n"
                        + "Thank you for visiting our website.", getFont("Data"));
                document.add(footer);
                document.close();
                return new ResponseEntity<>("{\"uuid\":\"" + filename + "\"}", HttpStatus.OK);
            }
            return CafeUtils.getResponeEntity("Required data not found", HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponeEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Bill>> getBills() {
        List<Bill> list = new ArrayList<>();
        if (jwtFilter.isAdmin()) {
            list = billDao.getAllBills();
        } else {
            list = billDao.getBillByUserName(jwtFilter.getCurrentUsername());
        }
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<org.springframework.data.domain.Page<Bill>> getBillsPaged(int page, int size, String sortBy, String direction) {
        org.springframework.data.domain.Pageable pageable = com.inn.cafe.utils.PageUtils.buildPageable(page, size, sortBy, direction);
        org.springframework.data.domain.Page<Bill> result = jwtFilter.isAdmin()
                ? billDao.getAllBillsPaged(pageable)
                : billDao.getBillByUserNamePaged(jwtFilter.getCurrentUsername(), pageable);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap) {
        log.info("Inside getPdf : requestMap {}", requestMap);
        try {
            byte[] byteArray = new byte[0];
            if (!requestMap.containsKey("uuid") && validateResquestMap(requestMap)) {
                return new ResponseEntity<>(byteArray, HttpStatus.BAD_REQUEST);
            }
            String filepath = storeLocation + File.separator + (String) requestMap.get("uuid") + ".pdf";

            if (CafeUtils.isFileExist(filepath)) {
                byteArray = getByteArray(filepath);
                return new ResponseEntity<>(byteArray, HttpStatus.OK);
            } else {
                requestMap.put("isGenerate", false);
                generateReport(requestMap);
                byteArray = getByteArray(filepath);
                return new ResponseEntity<>(byteArray, HttpStatus.OK);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public ResponseEntity<String> delete(Integer id) {
        if (!jwtFilter.isAdmin()) {
            throw new com.inn.cafe.exception.UnauthorizedException(CafeConstants.UNAUTHORIZED_ACCESS);
        }
        Optional optional = billDao.findById(id);
        if (optional.isEmpty()) {
            return CafeUtils.getResponeEntity("Bill id doesn't exist", HttpStatus.OK);
        }
        billDao.deleteById(id);
        return CafeUtils.getResponeEntity("Bill is deleted successfully", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Bill> updateOrderStatus(Integer id, com.inn.cafe.dto.OrderStatusUpdateRequest request) {
        if (!jwtFilter.isAdmin()) {
            throw new com.inn.cafe.exception.UnauthorizedException(CafeConstants.UNAUTHORIZED_ACCESS);
        }
        Bill bill = billDao.findById(id)
                .orElseThrow(() -> new com.inn.cafe.exception.ResourceNotFoundException("Bill not found with id: " + id));
        String status = request.getStatus() == null ? null : request.getStatus().trim().toUpperCase();
        if (!CafeConstants.VALID_ORDER_STATUSES.contains(status)) {
            throw new com.inn.cafe.exception.ValidationException("Invalid order status: " + request.getStatus());
        }
        bill.setOrderStatus(status);
        billDao.save(bill);
        log.info("Order status for bill {} updated to {}", bill.getUuid(), status);
        notificationService.notify(bill.getCreatedBy(), "Order Status Updated",
                "Your order " + bill.getUuid() + " is now " + status.replace('_', ' ') + ".",
                "ORDER_STATUS", bill.getId());
        return new ResponseEntity<>(bill, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Bill> cancelOrder(Integer id) {
        Bill bill = billDao.findById(id)
                .orElseThrow(() -> new com.inn.cafe.exception.ResourceNotFoundException("Bill not found with id: " + id));
        boolean owner = jwtFilter.isAdmin() || bill.getCreatedBy().equalsIgnoreCase(jwtFilter.getCurrentUsername());
        if (!owner) {
            throw new com.inn.cafe.exception.UnauthorizedException(CafeConstants.UNAUTHORIZED_ACCESS);
        }
        if (!CafeConstants.CUSTOMER_CANCELLABLE_STATUSES.contains(bill.getOrderStatus())) {
            throw new com.inn.cafe.exception.ValidationException(
                    "Order can no longer be cancelled (current status: " + bill.getOrderStatus() + ")");
        }
        bill.setOrderStatus(CafeConstants.ORDER_STATUS_CANCELLED);
        billDao.save(bill);
        reverseLoyaltyPoints(bill);
        log.info("Order {} cancelled by {}", bill.getUuid(), jwtFilter.getCurrentUsername());
        notificationService.notify(bill.getCreatedBy(), "Order Cancelled",
                "Your order " + bill.getUuid() + " has been cancelled.", "ORDER_STATUS", bill.getId());
        return new ResponseEntity<>(bill, HttpStatus.OK);
    }

    // Undoes any loyalty points earned/redeemed by this order so a cancellation doesn't leave
    // the customer's balance permanently skewed. No-op for legacy bills predating this column.
    private void reverseLoyaltyPoints(Bill bill) {
        int earned = bill.getLoyaltyPointsEarned() == null ? 0 : bill.getLoyaltyPointsEarned();
        int redeemed = bill.getLoyaltyPointsRedeemed() == null ? 0 : bill.getLoyaltyPointsRedeemed();
        if (earned == 0 && redeemed == 0) {
            return;
        }
        com.inn.cafe.POJO.User user = userDao.findByEmail(bill.getCreatedBy());
        if (user == null) {
            return;
        }
        int currentBalance = user.getLoyaltyPoints() == null ? 0 : user.getLoyaltyPoints();
        user.setLoyaltyPoints(currentBalance - earned + redeemed);
        userDao.save(user);
        bill.setLoyaltyPointsEarned(0);
        bill.setLoyaltyPointsRedeemed(0);
        billDao.save(bill);
    }

    private void insertBill(Map<String, Object> requestMap) {
        try {
            Bill bill = new Bill();
            bill.setUuid((String) requestMap.get("uuid"));
            bill.setName((String) requestMap.get("name"));
            bill.setEmail((String) requestMap.get("email"));
            bill.setContactNumber((String) requestMap.get("contactNumber"));
            bill.setPaymentMethod((String) requestMap.get("paymentMethod"));
            bill.setTotal(Integer.parseInt((String) requestMap.get("totalAmount")));
            bill.setProductDetails((String) requestMap.get("productDetails"));
            bill.setCreatedBy(jwtFilter.getCurrentUsername());
            billDao.save(bill);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean validateResquestMap(Map<String, Object> requestMap) {
        return requestMap.containsKey("name") &&
                requestMap.containsKey("contactNumber") &&
                requestMap.containsKey("email") &&
                requestMap.containsKey("paymentMethod") &&
                requestMap.containsKey("productDetails") &&
                requestMap.containsKey("totalAmount");
    }

    private void setRectaangleInPdf(Document document) throws DocumentException {
        log.info("Inside setRectaangleInPdf.");
        Rectangle rectangle = new Rectangle(577, 825, 18, 15);
        rectangle.enableBorderSide(1);
        rectangle.enableBorderSide(2);
        rectangle.enableBorderSide(4);
        rectangle.enableBorderSide(8);
        rectangle.setBorderColor(BaseColor.BLACK);
        rectangle.setBorderWidth(1);
        document.add(rectangle);
    }

    private Font getFont(String type) {
        log.info("Inside getFont");
        switch (type) {
            case "Header":
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 18, BaseColor.BLACK);
                headerFont.setStyle(Font.BOLD);
                return headerFont;
            case "Data":
                Font dareFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, BaseColor.BLACK);
                dareFont.setStyle(Font.BOLD);
                return dareFont;
            default:
                return new Font();
        }
    }

    private void addTableHeader(PdfPTable table) {
        log.info("Inside addTableHeader");
        Stream.of("Name", "Category", "Quantity", "Price", "Sub Total")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle));
                    header.setBackgroundColor(BaseColor.YELLOW);
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
                    header.setVerticalAlignment(Element.ALIGN_CENTER);
                    table.addCell(header);
                });
    }

    private void addRows(PdfPTable table, Map<String, Object> data) {
        log.info("Inside addRows");
        table.addCell((String) data.get("name"));
        table.addCell((String) data.get("category"));
        table.addCell((String) data.get("quantity"));
        table.addCell(Double.toString((Double) data.get("price")));
        table.addCell(Double.toString((Double) data.get("total")));
    }

    private byte[] getByteArray(String filepath) throws Exception {
        File initalFile = new File(filepath);
        InputStream targetStream = new FileInputStream(initalFile);
        byte[] byteArray = IOUtils.toByteArray(targetStream);
        targetStream.close();
        return byteArray;
    }

}
