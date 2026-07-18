package com.inn.cafe.dao;

import com.inn.cafe.POJO.User;
import com.inn.cafe.wrapper.UserWrapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;
import java.util.List;

public interface UserDao extends JpaRepository<User, Integer> {
    User findByEmailId(@Param("email") String email);


    List<UserWrapper> getAllUser();

    @Transactional
    @Modifying
    Integer updateStatus(@Param("status") String status, @Param("id") Integer id);

    List<String> getAllAdmin();

    User findByEmail(String email);

    @Query("select new com.inn.cafe.wrapper.UserWrapper(u.id, u.name, u.email, u.contactNumber, u.status) from User u where u.role = 'user'")
    Page<UserWrapper> getAllUserPaged(Pageable pageable);

    // Delivery module: list all delivery-partner accounts (role="delivery"), or just the
    // ones currently AVAILABLE for a new assignment.
    List<User> findByRole(String role);

    List<User> findByRoleAndDeliveryAvailability(String role, String deliveryAvailability);

}

