package com.inn.cafe.serviceImpl;

import com.google.common.base.Strings;
import com.inn.cafe.JWT.CustomerUserDetailsService;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Category;
import com.inn.cafe.POJO.Product;
import com.inn.cafe.constents.CafeConstants;
import com.inn.cafe.dao.CategoryDao;
import com.inn.cafe.dao.productDao;
import com.inn.cafe.dto.ProductSearchRequest;
import com.inn.cafe.exception.ResourceNotFoundException;
import com.inn.cafe.exception.UnauthorizedException;
import com.inn.cafe.exception.ValidationException;
import com.inn.cafe.service.productService;
import com.inn.cafe.specification.ProductSpecification;
import com.inn.cafe.utils.CafeUtils;
import com.inn.cafe.utils.EmailUtil;
import com.inn.cafe.utils.PageUtils;
import com.inn.cafe.wrapper.ProductWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class productServiceImpl implements productService {
    @Autowired
    productDao productDao;

    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    com.inn.cafe.JWT.jwtUtil jwtUtil;

    @Autowired
    JwtFilter jwtFilter;
    @Autowired
    CustomerUserDetailsService customerUserDetailsService;

    @Autowired
    EmailUtil emailUtil;

    @Override
    public ResponseEntity<String> addNewProduct(Map<String, String> requestMap) {
        log.info("Inside addNewProduct{}", requestMap);
        if (!jwtFilter.isAdmin()) {
            throw new UnauthorizedException(CafeConstants.UNAUTHORIZED_ACCESS);
        }
        if (!validateProductMap(requestMap, false)) {
            throw new ValidationException(CafeConstants.INVALID_DATA);
        }
        productDao.save(getProductFromMap(requestMap, false));
        return CafeUtils.getResponeEntity("Product Added Successfully", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getAllProduct() {
        return new ResponseEntity<>(productDao.getAllProduct(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Page<ProductWrapper>> getAllProductPaged(int page, int size, String sortBy, String direction) {
        return new ResponseEntity<>(productDao.getAllProductPaged(com.inn.cafe.utils.PageUtils.buildPageable(page, size, sortBy, direction)), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Page<ProductWrapper>> searchProducts(ProductSearchRequest request) {
        Page<Product> products = productDao.findAll(ProductSpecification.fromRequest(request),
                PageUtils.buildPageable(request.getPage(), request.getSize(), request.getSortBy(), request.getDirection()));
        return new ResponseEntity<>(products.map(this::toWrapper), HttpStatus.OK);
    }

    private ProductWrapper toWrapper(Product product) {
        return new ProductWrapper(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getstatus(),
                product.getIsVeg(),
                product.getSpicyLevel(),
                product.getBestSeller(),
                product.getNewArrival(),
                product.getRating(),
                product.getRatingCount(),
                product.getImageUrl(),
                product.getPrepTimeMinutes()
        );
    }


    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        if (!jwtFilter.isAdmin()) {
            throw new UnauthorizedException(CafeConstants.UNAUTHORIZED_ACCESS);
        }
        if (!validateProductMap(requestMap, true)) {
            throw new ValidationException(CafeConstants.INVALID_DATA);
        }
        Optional<Product> optional = productDao.findById(Integer.parseInt(requestMap.get("id")));
        if (optional.isEmpty()) {
            return CafeUtils.getResponeEntity("Product id doesn't exist", HttpStatus.OK);
        }
        Product updated = getProductFromMap(requestMap, true);
        // Preserve the existing active/inactive status here; it is only ever changed via the
        // dedicated updateProductStatus endpoint, not as a side effect of a details edit.
        updated.setstatus(optional.get().getstatus());
        productDao.save(updated);
        return CafeUtils.getResponeEntity("Product is updated successfully", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> delete(Integer id) {
        if (!jwtFilter.isAdmin()) {
            throw new UnauthorizedException(CafeConstants.UNAUTHORIZED_ACCESS);
        }
        Optional optional = productDao.findById(id);
        if (optional.isEmpty()) {
            return CafeUtils.getResponeEntity("Product id doesn't exist", HttpStatus.OK);
        }
        productDao.deleteById(id);
        return CafeUtils.getResponeEntity("Product is deleted successfully", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getByCategory(Integer id) {
        return new ResponseEntity<>(productDao.getByCategory(id), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ProductWrapper> getProductById(Integer id) {
        return new ResponseEntity<>(productDao.getProductById(id), HttpStatus.OK);
    }

    @Modifying
    @Transactional
    @Override
    public ResponseEntity<String> updateProductStatus(Map<String, String> requestMap) {
        if (!jwtFilter.isAdmin()) {
            throw new UnauthorizedException(CafeConstants.UNAUTHORIZED_ACCESS);
        }
        Optional optional = productDao.findById(Integer.parseInt(requestMap.get("id")));
        if (optional.isEmpty()) {
            return CafeUtils.getResponeEntity("Product id doesn't exist", HttpStatus.OK);
        }
        productDao.updateProductStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
        return CafeUtils.getResponeEntity("Product status is updated successfully", HttpStatus.OK);
    }


    private boolean validateProductMap(Map<String, String> requestMap, boolean validateId) {
        if (requestMap.containsKey("name")) {
            if (requestMap.containsKey("id") && validateId) {
                return true;
            } else if (!validateId) {
                return true;
            }
        }
        return false;
    }

    private Product getProductFromMap(Map<String, String> requestMap, boolean isAdd) {
        Product product = new Product();
        Category category = new Category();
        category.setId(Integer.parseInt(requestMap.get("categoryId")));

        if (isAdd) {
            product.setId(Integer.parseInt(requestMap.get("id")));
        } else {
            product.setstatus("true");
        }
        product.setCategory(category);
        product.setName(requestMap.get("name"));
        product.setDescription(requestMap.get("description"));
        product.setPrice(Integer.parseInt(requestMap.get("price")));

        product.setIsVeg(parseBooleanOrDefault(requestMap.get("isVeg"), true));
        product.setSpicyLevel(requestMap.getOrDefault("spicyLevel", "NONE"));
        product.setBestSeller(parseBooleanOrDefault(requestMap.get("bestSeller"), false));
        product.setNewArrival(parseBooleanOrDefault(requestMap.get("newArrival"), false));
        product.setImageUrl(requestMap.get("imageUrl"));
        if (requestMap.get("prepTimeMinutes") != null && !requestMap.get("prepTimeMinutes").isBlank()) {
            product.setPrepTimeMinutes(Integer.parseInt(requestMap.get("prepTimeMinutes")));
        }

        return product;
    }

    private Boolean parseBooleanOrDefault(String value, boolean defaultValue) {
        return value == null || value.isBlank() ? defaultValue : Boolean.parseBoolean(value);
    }
}

