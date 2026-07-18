package com.inn.cafe.dao;

import com.inn.cafe.POJO.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreDao extends JpaRepository<Store, Integer> {
    List<Store> findByActiveTrue();
}
