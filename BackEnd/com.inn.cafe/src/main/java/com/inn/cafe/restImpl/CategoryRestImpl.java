package com.inn.cafe.restImpl;

import com.inn.cafe.POJO.Category;
import com.inn.cafe.dao.CategoryDao;
import com.inn.cafe.rest.CategoryRest;
import com.inn.cafe.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class CategoryRestImpl implements CategoryRest {
    @Autowired
    CategoryService categoryService;

    @Autowired
    CategoryDao categoryDao;

    @Override
    public ResponseEntity<String> addNewCategory(Map<String, String> requestMap) {
        return categoryService.addNewCategory(requestMap);
    }

    @Override
    public ResponseEntity<List<Category>> getAllCategory(String Value) {
        return categoryService.getAllCategory(Value);
    }

    @Override
    public ResponseEntity<org.springframework.data.domain.Page<Category>> getAllCategoryPaged(int page, int size, String sortBy, String direction) {
        return categoryService.getAllCategoryPaged(page, size, sortBy, direction);
    }

    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        return categoryService.update(requestMap);
    }


}

