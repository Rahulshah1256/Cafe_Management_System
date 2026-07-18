package com.inn.cafe.dao;

import com.inn.cafe.POJO.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BillDao extends JpaRepository<Bill, Integer> {
    List<Bill> getAllBills();
    List<Bill> getBillByUserName(@Param("username") String username);

    @Query("select b from Bill b order by b.id desc")
    Page<Bill> getAllBillsPaged(Pageable pageable);

    @Query("select b from Bill b where b.createdBy=:username order by b.id desc")
    Page<Bill> getBillByUserNamePaged(@Param("username") String username, Pageable pageable);

    Optional<Bill> findByUuid(String uuid);

    // Kitchen Dashboard: live queue of orders still in progress, oldest first so staff
    // work through orders in the sequence they were received.
    List<Bill> findByOrderStatusInOrderByCreatedAtAsc(Collection<String> orderStatuses);

    // Delivery module: a rider's active deliveries, oldest first.
    List<Bill> findByAssignedDeliveryPartnerAndOrderStatusInOrderByCreatedAtAsc(
            String assignedDeliveryPartner, Collection<String> orderStatuses);
}

