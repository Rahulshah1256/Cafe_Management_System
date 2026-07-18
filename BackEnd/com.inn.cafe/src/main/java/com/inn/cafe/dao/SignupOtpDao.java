package com.inn.cafe.dao;

import com.inn.cafe.POJO.SignupOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SignupOtpDao extends JpaRepository<SignupOtp, Integer> {
    Optional<SignupOtp> findByEmail(String email);

    void deleteByEmail(String email);
}
