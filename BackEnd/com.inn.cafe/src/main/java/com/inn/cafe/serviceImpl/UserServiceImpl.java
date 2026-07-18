package com.inn.cafe.serviceImpl;

import com.google.common.base.Strings;
import com.inn.cafe.JWT.CustomerUserDetailsService;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.JWT.jwtUtil;
import com.inn.cafe.POJO.SignupOtp;
import com.inn.cafe.POJO.User;
import com.inn.cafe.constents.CafeConstants;
import com.inn.cafe.dao.SignupOtpDao;
import com.inn.cafe.dao.UserDao;
import com.inn.cafe.dto.LoginRequest;
import com.inn.cafe.dto.SignUpRequest;
import com.inn.cafe.dto.VerifySignupOtpRequest;
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

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserDao userDao;

    @Autowired
    SignupOtpDao signupOtpDao;

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
        User existingUser = userDao.findByEmailId(request.getEmail());
        if (!Objects.isNull(existingUser)) {
            return CafeUtils.getResponeEntity("Email already exits.", HttpStatus.BAD_REQUEST);
        }

        // Two-step signup: don't create the User row yet - stash the requested details plus
        // a fresh OTP in signup_otp, and only materialize the account once the OTP is
        // verified via /user/verifySignupOtp. This guarantees every registered account has
        // a real, deliverable email address behind it.
        String otp = CafeUtils.generateOtp();
        SignupOtp pending = signupOtpDao.findByEmail(request.getEmail()).orElseGet(SignupOtp::new);
        pending.setEmail(request.getEmail());
        pending.setName(request.getName());
        pending.setContactNumber(request.getContactNumber());
        pending.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        pending.setRequestedStatus(request.getStatus());
        pending.setOtpHash(passwordEncoder.encode(otp));
        pending.setAttempts(0);
        pending.setExpiresAt(LocalDateTime.now().plusMinutes(CafeConstants.SIGNUP_OTP_VALID_MINUTES));
        if (pending.getCreatedAt() == null) {
            pending.setCreatedAt(LocalDateTime.now());
        }
        signupOtpDao.save(pending);

        try {
            emailUtil.sendOtpMail(request.getEmail(), otp, CafeConstants.SIGNUP_OTP_VALID_MINUTES);
        } catch (jakarta.mail.MessagingException ex) {
            throw new com.inn.cafe.exception.CafeException("Failed to send verification email", ex);
        }
        log.info("Signup OTP sent to {}", request.getEmail());
        return CafeUtils.getResponeEntity("OTP sent to your email. Please verify to complete registration.", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> verifySignupOtp(VerifySignupOtpRequest request) {
        log.info("Inside verifySignupOtp {}", request.getEmail());
        Optional<SignupOtp> optionalPending = signupOtpDao.findByEmail(request.getEmail());
        if (optionalPending.isEmpty()) {
            return CafeUtils.getResponeEntity("No pending registration found for this email. Please sign up again.", HttpStatus.BAD_REQUEST);
        }
        SignupOtp pending = optionalPending.get();

        if (pending.getExpiresAt().isBefore(LocalDateTime.now())) {
            signupOtpDao.delete(pending);
            return CafeUtils.getResponeEntity("OTP expired. Please sign up again.", HttpStatus.BAD_REQUEST);
        }
        if (pending.getAttempts() >= CafeConstants.SIGNUP_OTP_MAX_ATTEMPTS) {
            signupOtpDao.delete(pending);
            return CafeUtils.getResponeEntity("Too many incorrect attempts. Please sign up again.", HttpStatus.BAD_REQUEST);
        }
        if (!passwordEncoder.matches(request.getOtp(), pending.getOtpHash())) {
            pending.setAttempts(pending.getAttempts() + 1);
            signupOtpDao.save(pending);
            int remaining = CafeConstants.SIGNUP_OTP_MAX_ATTEMPTS - pending.getAttempts();
            return CafeUtils.getResponeEntity("Incorrect OTP. " + remaining + " attempt(s) remaining.", HttpStatus.BAD_REQUEST);
        }

        // Double-check the email wasn't registered by another concurrent request in the
        // meantime before finally creating the account.
        if (!Objects.isNull(userDao.findByEmailId(pending.getEmail()))) {
            signupOtpDao.delete(pending);
            return CafeUtils.getResponeEntity("Email already exits.", HttpStatus.BAD_REQUEST);
        }

        User user = new User();
        user.setName(pending.getName());
        user.setContactNumber(pending.getContactNumber());
        user.setEmail(pending.getEmail());
        user.setPassword(pending.getPasswordHash());
        user.setStatus(pending.getRequestedStatus());
        user.setRole("user");
        userDao.save(user);
        signupOtpDao.delete(pending);
        log.info("Email verified and account created for {}", pending.getEmail());
        return CafeUtils.getResponeEntity("Email verified successfully! Your account has been created. You can now log in.", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> resendSignupOtp(Map<String, String> requestMap) {
        String email = requestMap.get("email");
        log.info("Inside resendSignupOtp {}", email);
        Optional<SignupOtp> optionalPending = Strings.isNullOrEmpty(email)
                ? Optional.empty()
                : signupOtpDao.findByEmail(email);
        if (optionalPending.isEmpty()) {
            return CafeUtils.getResponeEntity("No pending registration found for this email. Please sign up again.", HttpStatus.BAD_REQUEST);
        }
        SignupOtp pending = optionalPending.get();
        String otp = CafeUtils.generateOtp();
        pending.setOtpHash(passwordEncoder.encode(otp));
        pending.setAttempts(0);
        pending.setExpiresAt(LocalDateTime.now().plusMinutes(CafeConstants.SIGNUP_OTP_VALID_MINUTES));
        signupOtpDao.save(pending);

        try {
            emailUtil.sendOtpMail(email, otp, CafeConstants.SIGNUP_OTP_VALID_MINUTES);
        } catch (jakarta.mail.MessagingException ex) {
            throw new com.inn.cafe.exception.CafeException("Failed to send verification email", ex);
        }
        log.info("Signup OTP resent to {}", email);
        return CafeUtils.getResponeEntity("A new OTP has been sent to your email.", HttpStatus.OK);
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
