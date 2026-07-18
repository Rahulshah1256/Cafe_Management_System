package com.inn.cafe.serviceImpl;

import com.google.common.base.Strings;
import com.inn.cafe.JWT.CustomerUserDetailsService;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.JWT.jwtUtil;
import com.inn.cafe.POJO.User;
import com.inn.cafe.constents.CafeConstants;
import com.inn.cafe.dao.UserDao;
import com.inn.cafe.dto.LoginRequest;
import com.inn.cafe.dto.SignUpRequest;
import com.inn.cafe.exception.UnauthorizedException;
import com.inn.cafe.service.UserService;
import com.inn.cafe.utils.CafeUtils;
import com.inn.cafe.utils.EmailUtil;
import com.inn.cafe.wrapper.UserWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserDao userDao;

    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    jwtUtil jwtUtil;

    @Autowired
    JwtFilter jwtFilter;
    @Autowired
    CustomerUserDetailsService customerUserDetailsService;

    @Autowired
    EmailUtil emailUtil;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public ResponseEntity<String> signUp(SignUpRequest request) {
        log.info("Inside signup {}", request.getEmail());
        User user = userDao.findByEmailId(request.getEmail());
        if (Objects.isNull(user)) {
            userDao.save(getUserFromRequest(request));
            return CafeUtils.getResponeEntity("Successfully  Registered.", HttpStatus.OK);
        }
        return CafeUtils.getResponeEntity("Email already exits.", HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<String> login(LoginRequest request) {
        log.info("Inside login {}", request.getEmail());
        try {
            Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            if (auth.isAuthenticated()) {
                if (customerUserDetailsService.getUserDatails().getStatus().equalsIgnoreCase("true")) {
                    return new ResponseEntity<String>("{\"token\":\"" + jwtUtil.generateToken(
                            customerUserDetailsService.getUserDatails().getEmail(), customerUserDetailsService.getUserDatails().getRole()) + "\"}",
                            HttpStatus.OK);
                } else {
                    return new ResponseEntity<String>("{\"message\":\"" + "Wait for Admin Approvel." + "\"}",
                            HttpStatus.BAD_REQUEST);
                }
            }
        } catch (Exception ex) {
            log.warn("Login failed for {}: {}", request.getEmail(), ex.getMessage());
        }
        return new ResponseEntity<String>("{\"message\":\"" + "Bad Credentials." + "\"}",
                HttpStatus.BAD_REQUEST);
    }

    private User getUserFromRequest(SignUpRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setContactNumber(request.getContactNumber());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(request.getStatus());
        user.setRole("user");
        return user;
    }

    @Override
    public ResponseEntity<List<UserWrapper>> getAllUser() {
        if (!jwtFilter.isAdmin()) {
            throw new UnauthorizedException(CafeConstants.UNAUTHORIZED_ACCESS);
        }
        return new ResponseEntity<>(userDao.getAllUser(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<org.springframework.data.domain.Page<UserWrapper>> getAllUserPaged(int page, int size, String sortBy, String direction) {
        if (!jwtFilter.isAdmin()) {
            throw new UnauthorizedException(CafeConstants.UNAUTHORIZED_ACCESS);
        }
        return new ResponseEntity<>(userDao.getAllUserPaged(com.inn.cafe.utils.PageUtils.buildPageable(page, size, sortBy, direction)), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        if (!jwtFilter.isAdmin()) {
            throw new UnauthorizedException(CafeConstants.UNAUTHORIZED_ACCESS);
        }
        Optional<User> optional = userDao.findById(Integer.parseInt(requestMap.get("id")));
        if (optional.isEmpty()) {
            return CafeUtils.getResponeEntity("User id doesn't exist", HttpStatus.OK);
        }
        userDao.updateStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
        sendMailToAllAdmin(requestMap.get("status"), optional.get().getEmail(), userDao.getAllAdmin());
        return CafeUtils.getResponeEntity("User Status is updated Successfully", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> checkToken() {
        return CafeUtils.getResponeEntity("true", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
        User user = userDao.findByEmail(jwtFilter.getCurrentUsername());
        if (Objects.isNull(user)) {
            return CafeUtils.getResponeEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (!passwordEncoder.matches(requestMap.get("oldPassword"), user.getPassword())) {
            return CafeUtils.getResponeEntity("Incorrect Old Password", HttpStatus.BAD_REQUEST);
        }
        user.setPassword(passwordEncoder.encode(requestMap.get("newPassword")));
        userDao.save(user);
        return CafeUtils.getResponeEntity("Password Updated Successfully", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> forgetPassword(Map<String, String> requestMap) {
        User user = userDao.findByEmail(requestMap.get("email"));
        if (Objects.isNull(user) || Strings.isNullOrEmpty(user.getEmail())) {
            return CafeUtils.getResponeEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        // Passwords are now hashed (BCrypt) and cannot be recovered, so issue a fresh
        // temporary password, persist its hash, and email the plaintext temp password.
        String temporaryPassword = CafeUtils.generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        userDao.save(user);
        try {
            emailUtil.forgetMail(user.getEmail(), "Credentials by Cafe Management System", temporaryPassword);
        } catch (jakarta.mail.MessagingException ex) {
            throw new com.inn.cafe.exception.CafeException("Failed to send credentials email", ex);
        }
        return CafeUtils.getResponeEntity("Check Your mail for Credentials", HttpStatus.OK);
    }


    private void sendMailToAllAdmin(String status, String user, List<String> allAdmin) {
        allAdmin.remove(jwtFilter.getCurrentUsername());
        if (status != null && status.equalsIgnoreCase("true")) {
            emailUtil.SendSimpleMessage(jwtFilter.getCurrentUsername(), "Account Approved", "USER:- " + user + "\n is approved by\nADMIN:-" + jwtFilter.getCurrentUsername(), allAdmin);
        } else {
            emailUtil.SendSimpleMessage(jwtFilter.getCurrentUsername(), "Account Disabled", "USER:- " + user + "\n is disabled by\nADMIN:-" + jwtFilter.getCurrentUsername(), allAdmin);

        }
    }

}
