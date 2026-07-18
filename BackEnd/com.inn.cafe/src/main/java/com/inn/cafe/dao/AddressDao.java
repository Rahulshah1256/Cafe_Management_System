package com.inn.cafe.dao;

import com.inn.cafe.POJO.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressDao extends JpaRepository<Address, Integer> {
    List<Address> findByUserIdOrderByIsDefaultDescIdDesc(Integer userId);
}
