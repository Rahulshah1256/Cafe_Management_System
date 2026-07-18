package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.CustomerUserDetailsService;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Category;
import com.inn.cafe.dao.CategoryDao;
import com.inn.cafe.exception.UnauthorizedException;
import com.inn.cafe.exception.ValidationException;
import com.inn.cafe.utils.EmailUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryDao categoryDao;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private com.inn.cafe.JWT.jwtUtil jwtUtil;
    @Mock
    private JwtFilter jwtFilter;
    @Mock
    private CustomerUserDetailsService customerUserDetailsService;
    @Mock
    private EmailUtil emailUtil;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void addNewCategory_shouldThrowUnauthorized_whenCallerIsNotAdmin() {
        when(jwtFilter.isAdmin()).thenReturn(false);
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("name", "Pizza");

        assertThrows(UnauthorizedException.class, () -> categoryService.addNewCategory(requestMap));
        verify(categoryDao, never()).save(any(Category.class));
    }

    @Test
    void addNewCategory_shouldThrowValidationException_whenNameMissing() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        Map<String, String> requestMap = new HashMap<>();

        assertThrows(ValidationException.class, () -> categoryService.addNewCategory(requestMap));
        verify(categoryDao, never()).save(any(Category.class));
    }

    @Test
    void addNewCategory_shouldSaveCategory_whenValidRequestFromAdmin() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("name", "Pizza");

        ResponseEntity<String> response = categoryService.addNewCategory(requestMap);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Category Added Successfully"));
        verify(categoryDao, times(1)).save(any(Category.class));
    }

    @Test
    void update_shouldReturnOkWithDoesNotExistMessage_whenCategoryIdMissing() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        when(categoryDao.findById(99)).thenReturn(Optional.empty());

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("id", "99");
        requestMap.put("name", "Updated");

        ResponseEntity<String> response = categoryService.update(requestMap);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("doesn't exist"));
        verify(categoryDao, never()).save(any(Category.class));
    }

    @Test
    void getAllCategoryPaged_shouldReturnPagedResult() {
        Category category = new Category();
        category.setId(1);
        category.setName("Pizza");
        Page<Category> page = new PageImpl<>(Collections.singletonList(category));
        when(categoryDao.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        ResponseEntity<Page<Category>> response = categoryService.getAllCategoryPaged(0, 10, "id", "asc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }
}
