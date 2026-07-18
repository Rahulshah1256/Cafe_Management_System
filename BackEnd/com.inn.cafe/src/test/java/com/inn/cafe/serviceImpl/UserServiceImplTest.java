package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.CustomerUserDetailsService;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.JWT.jwtUtil;
import com.inn.cafe.POJO.SignupOtp;
import com.inn.cafe.POJO.User;
import com.inn.cafe.dao.SignupOtpDao;
import com.inn.cafe.dao.UserDao;
import com.inn.cafe.dto.LoginRequest;
import com.inn.cafe.dto.SignUpRequest;
import com.inn.cafe.dto.VerifySignupOtpRequest;
import com.inn.cafe.exception.UnauthorizedException;
import com.inn.cafe.utils.EmailUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDao userDao;
    @Mock
    private SignupOtpDao signupOtpDao;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private jwtUtil jwtUtil;
    @Mock
    private JwtFilter jwtFilter;
    @Mock
    private CustomerUserDetailsService customerUserDetailsService;
    @Mock
    private EmailUtil emailUtil;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private SignUpRequest signUpRequest;

    @BeforeEach
    void setUp() {
        signUpRequest = new SignUpRequest();
        signUpRequest.setName("Test User");
        signUpRequest.setEmail("newuser@cafe.com");
        signUpRequest.setPassword("password1");
        signUpRequest.setContactNumber("9876543210");
        signUpRequest.setStatus("true");
    }

    @Test
    void signUp_shouldSendOtpAndStorePendingRegistration_whenEmailNotTaken() throws Exception {
        when(userDao.findByEmailId("newuser@cafe.com")).thenReturn(null);
        when(signupOtpDao.findByEmail("newuser@cafe.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-value");

        ResponseEntity<String> response = userService.signUp(signUpRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("OTP sent"));
        verify(signupOtpDao, times(1)).save(any(SignupOtp.class));
        verify(emailUtil, times(1)).sendOtpMail(eq("newuser@cafe.com"), anyString(), anyInt());
        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void signUp_shouldReturnBadRequest_whenEmailAlreadyExists() {
        when(userDao.findByEmailId("newuser@cafe.com")).thenReturn(new User());

        ResponseEntity<String> response = userService.signUp(signUpRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Email already exits."));
        verify(signupOtpDao, never()).save(any(SignupOtp.class));
    }

    @Test
    void verifySignupOtp_shouldCreateUser_whenOtpCorrect() {
        SignupOtp pending = new SignupOtp();
        pending.setEmail("newuser@cafe.com");
        pending.setName("Test User");
        pending.setContactNumber("9876543210");
        pending.setPasswordHash("hashed-password");
        pending.setRequestedStatus("true");
        pending.setOtpHash("hashed-otp");
        pending.setAttempts(0);
        pending.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        when(signupOtpDao.findByEmail("newuser@cafe.com")).thenReturn(Optional.of(pending));
        when(passwordEncoder.matches("123456", "hashed-otp")).thenReturn(true);
        when(userDao.findByEmailId("newuser@cafe.com")).thenReturn(null);

        VerifySignupOtpRequest request = new VerifySignupOtpRequest();
        request.setEmail("newuser@cafe.com");
        request.setOtp("123456");

        ResponseEntity<String> response = userService.verifySignupOtp(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("verified successfully"));
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDao, times(1)).save(userCaptor.capture());
        assertEquals("newuser@cafe.com", userCaptor.getValue().getEmail());
        assertEquals("hashed-password", userCaptor.getValue().getPassword());
        verify(signupOtpDao, times(1)).delete(pending);
    }

    @Test
    void verifySignupOtp_shouldReturnBadRequest_whenOtpIncorrect() {
        SignupOtp pending = new SignupOtp();
        pending.setEmail("newuser@cafe.com");
        pending.setOtpHash("hashed-otp");
        pending.setAttempts(0);
        pending.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        when(signupOtpDao.findByEmail("newuser@cafe.com")).thenReturn(Optional.of(pending));
        when(passwordEncoder.matches("000000", "hashed-otp")).thenReturn(false);

        VerifySignupOtpRequest request = new VerifySignupOtpRequest();
        request.setEmail("newuser@cafe.com");
        request.setOtp("000000");

        ResponseEntity<String> response = userService.verifySignupOtp(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Incorrect OTP"));
        verify(userDao, never()).save(any(User.class));
        verify(signupOtpDao, times(1)).save(pending);
        assertEquals(1, pending.getAttempts());
    }

    @Test
    void verifySignupOtp_shouldReturnBadRequest_whenOtpExpired() {
        SignupOtp pending = new SignupOtp();
        pending.setEmail("newuser@cafe.com");
        pending.setOtpHash("hashed-otp");
        pending.setAttempts(0);
        pending.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(signupOtpDao.findByEmail("newuser@cafe.com")).thenReturn(Optional.of(pending));

        VerifySignupOtpRequest request = new VerifySignupOtpRequest();
        request.setEmail("newuser@cafe.com");
        request.setOtp("123456");

        ResponseEntity<String> response = userService.verifySignupOtp(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("expired"));
        verify(signupOtpDao, times(1)).delete(pending);
        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void verifySignupOtp_shouldReturnBadRequest_whenNoPendingRegistration() {
        when(signupOtpDao.findByEmail("missing@cafe.com")).thenReturn(Optional.empty());

        VerifySignupOtpRequest request = new VerifySignupOtpRequest();
        request.setEmail("missing@cafe.com");
        request.setOtp("123456");

        ResponseEntity<String> response = userService.verifySignupOtp(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("No pending registration"));
    }

    @Test
    void login_shouldReturnBadRequest_whenCredentialsInvalid() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("user@cafe.com");
        loginRequest.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseEntity<String> response = userService.login(loginRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Bad Credentials."));
    }

    @Test
    void login_shouldReturnToken_whenCredentialsValidAndUserActive() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("user@cafe.com");
        loginRequest.setPassword("password1");

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        User activeUser = new User();
        activeUser.setEmail("user@cafe.com");
        activeUser.setRole("user");
        activeUser.setStatus("true");
        when(customerUserDetailsService.getUserDatails()).thenReturn(activeUser);
        when(jwtUtil.generateToken("user@cafe.com", "user")).thenReturn("dummy-jwt-token");

        ResponseEntity<String> response = userService.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("dummy-jwt-token"));
    }

    @Test
    void getAllUser_shouldThrowUnauthorized_whenCallerIsNotAdmin() {
        when(jwtFilter.isAdmin()).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> userService.getAllUser());
        verify(userDao, never()).getAllUser();
    }

    @Test
    void changePassword_shouldReturnBadRequest_whenOldPasswordIncorrect() {
        User user = new User();
        user.setEmail("user@cafe.com");
        user.setPassword("hashed-old-password");
        when(jwtFilter.getCurrentUsername()).thenReturn("user@cafe.com");
        when(userDao.findByEmail("user@cafe.com")).thenReturn(user);
        when(passwordEncoder.matches("wrongOldPassword", "hashed-old-password")).thenReturn(false);

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("oldPassword", "wrongOldPassword");
        requestMap.put("newPassword", "newPassword1");

        ResponseEntity<String> response = userService.changePassword(requestMap);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Incorrect Old Password"));
        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void changePassword_shouldUpdatePassword_whenOldPasswordCorrect() {
        User user = new User();
        user.setEmail("user@cafe.com");
        user.setPassword("hashed-old-password");
        when(jwtFilter.getCurrentUsername()).thenReturn("user@cafe.com");
        when(userDao.findByEmail("user@cafe.com")).thenReturn(user);
        when(passwordEncoder.matches("oldPassword1", "hashed-old-password")).thenReturn(true);
        when(passwordEncoder.encode("newPassword1")).thenReturn("hashed-new-password");

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("oldPassword", "oldPassword1");
        requestMap.put("newPassword", "newPassword1");

        ResponseEntity<String> response = userService.changePassword(requestMap);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("hashed-new-password", user.getPassword());
        verify(userDao, times(1)).save(user);
    }
}
