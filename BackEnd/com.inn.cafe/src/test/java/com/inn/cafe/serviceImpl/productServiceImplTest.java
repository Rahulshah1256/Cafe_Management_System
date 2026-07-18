package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.CustomerUserDetailsService;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Category;
import com.inn.cafe.POJO.Product;
import com.inn.cafe.dao.productDao;
import com.inn.cafe.dto.ProductSearchRequest;
import com.inn.cafe.exception.UnauthorizedException;
import com.inn.cafe.exception.ValidationException;
import com.inn.cafe.utils.EmailUtil;
import com.inn.cafe.wrapper.ProductWrapper;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class productServiceImplTest {

    @Mock
    private productDao productDao;
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
    private productServiceImpl productService;

    private Map<String, String> validProductMap() {
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("name", "Margherita Pizza");
        requestMap.put("description", "Classic cheese pizza");
        requestMap.put("price", "250");
        requestMap.put("categoryId", "1");
        return requestMap;
    }

    @Test
    void addNewProduct_shouldThrowUnauthorized_whenCallerIsNotAdmin() {
        when(jwtFilter.isAdmin()).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> productService.addNewProduct(validProductMap()));
        verify(productDao, never()).save(any(Product.class));
    }

    @Test
    void addNewProduct_shouldThrowValidationException_whenNameMissing() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        Map<String, String> requestMap = validProductMap();
        requestMap.remove("name");

        assertThrows(ValidationException.class, () -> productService.addNewProduct(requestMap));
        verify(productDao, never()).save(any(Product.class));
    }

    @Test
    void addNewProduct_shouldSaveProduct_whenValidRequestFromAdmin() {
        when(jwtFilter.isAdmin()).thenReturn(true);

        ResponseEntity<String> response = productService.addNewProduct(validProductMap());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Product Added Successfully"));
        verify(productDao, times(1)).save(any(Product.class));
    }

    @Test
    void delete_shouldReturnOkWithDoesNotExistMessage_whenProductIdMissing() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        when(productDao.findById(42)).thenReturn(Optional.empty());

        ResponseEntity<String> response = productService.delete(42);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("doesn't exist"));
        verify(productDao, never()).deleteById(anyInt());
    }

    @Test
    void delete_shouldDeleteProduct_whenProductExistsAndCallerIsAdmin() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        Product product = new Product();
        product.setId(42);
        when(productDao.findById(42)).thenReturn(Optional.of(product));

        ResponseEntity<String> response = productService.delete(42);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("deleted successfully"));
        verify(productDao, times(1)).deleteById(42);
    }

    @Test
    void getProductById_shouldReturnWrapper() {
        ProductWrapper wrapper = new ProductWrapper(1, "Margherita Pizza", "desc", 250, 1, "Pizza", "true");
        when(productDao.getProductById(1)).thenReturn(wrapper);

        ResponseEntity<ProductWrapper> response = productService.getProductById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Margherita Pizza", response.getBody().getName());
    }

    @Test
    void searchProducts_shouldMapEntitiesToWrappers() {
        Category category = new Category();
        category.setId(1);
        category.setName("Pizza");
        Product product = new Product();
        product.setId(1);
        product.setName("Margherita Pizza");
        product.setDescription("Classic cheese pizza");
        product.setPrice(250);
        product.setCategory(category);
        product.setstatus("true");
        product.setIsVeg(true);
        product.setBestSeller(true);

        Page<Product> page = new PageImpl<>(Collections.singletonList(product));
        when(productDao.findAll(any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        ProductSearchRequest request = new ProductSearchRequest();
        request.setIsVeg(true);
        request.setBestSeller(true);

        ResponseEntity<Page<ProductWrapper>> response = productService.searchProducts(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
        ProductWrapper wrapper = response.getBody().getContent().get(0);
        assertEquals("Margherita Pizza", wrapper.getName());
        assertEquals("Pizza", wrapper.getCategoryName());
        assertTrue(wrapper.getIsVeg());
        assertTrue(wrapper.getBestSeller());
    }

    @Test
    void update_shouldPreserveExistingStatus_notResetIt() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        Product existing = new Product();
        existing.setId(1);
        existing.setstatus("false");
        when(productDao.findById(1)).thenReturn(Optional.of(existing));

        Map<String, String> requestMap = validProductMap();
        requestMap.put("id", "1");

        ResponseEntity<String> response = productService.update(requestMap);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productDao, times(1)).save(argThat(p -> "false".equals(p.getstatus())));
    }
}
