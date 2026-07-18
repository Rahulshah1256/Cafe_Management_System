package com.inn.cafe.serviceImpl;

import com.google.common.base.Strings;
import com.inn.cafe.JWT.CustomerUserDetailsService;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Category;
import com.inn.cafe.constents.CafeConstants;
import com.inn.cafe.dao.CategoryDao;
import com.inn.cafe.exception.UnauthorizedException;
import com.inn.cafe.exception.ValidationException;
import com.inn.cafe.service.CategoryService;
import com.inn.cafe.utils.CafeUtils;
import com.inn.cafe.utils.EmailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    CategoryDao categoryDao;

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
    public ResponseEntity<String> addNewCategory(Map<String, String> requestMap) {
        log.info("Inside addNewCategory{}", requestMap);
        if (!jwtFilter.isAdmin()) {
            throw new UnauthorizedException(CafeConstants.UNAUTHORIZED_ACCESS);
        }
        if (!validateCategoryMap(requestMap, false)) {
            throw new ValidationException(CafeConstants.INVALID_DATA);
        }
        categoryDao.save(getCategoryFromMap(requestMap, false));
        return CafeUtils.getResponeEntity("Category Added Successfully", HttpStatus.OK);
    }

    private boolean validateCategoryMap(Map<String, String> requestMap, boolean validateId) {
        if (requestMap.containsKey("name")) {
            if(requestMap.containsKey("id") && validateId){
                return true;
            }else if(!validateId){
                return true;
            }
        }
        return false;
    }
    private Category getCategoryFromMap(Map<String, String> requestMap, boolean isAdd) {
        Category category = new Category();
        if(isAdd){
            category.setId(Integer.parseInt(requestMap.get("id")));
        }
        category.setName(requestMap.get("name"));
        return category;
    }

    @Override
    public ResponseEntity<List<Category>> getAllCategory(String Value) {
        if(!Strings.isNullOrEmpty(Value) && Value.equalsIgnoreCase("true")) {
            return new ResponseEntity<List<Category>>(new ArrayList<>(), HttpStatus.OK);
        }
        return new ResponseEntity<>(categoryDao.findAll(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<org.springframework.data.domain.Page<Category>> getAllCategoryPaged(int page, int size, String sortBy, String direction) {
        return new ResponseEntity<>(categoryDao.findAll(com.inn.cafe.utils.PageUtils.buildPageable(page, size, sortBy, direction)), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        if (!jwtFilter.isAdmin()) {
            throw new UnauthorizedException(CafeConstants.UNAUTHORIZED_ACCESS);
        }
        if (!validateCategoryMap(requestMap, true)) {
            throw new ValidationException(CafeConstants.INVALID_DATA);
        }
        Optional optional = categoryDao.findById(Integer.parseInt(requestMap.get("id")));
        if (optional.isEmpty()) {
            return CafeUtils.getResponeEntity("Category id doesn't exist", HttpStatus.OK);
        }
        categoryDao.save(getCategoryFromMap(requestMap, true));
        return CafeUtils.getResponeEntity("Category is updated successfully", HttpStatus.OK);
    }
}

