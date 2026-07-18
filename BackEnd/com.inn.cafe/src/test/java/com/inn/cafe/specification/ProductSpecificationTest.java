package com.inn.cafe.specification;

import com.inn.cafe.POJO.Product;
import com.inn.cafe.dto.ProductSearchRequest;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Verifies ProductSpecification.fromRequest() wires the expected Criteria API predicates
 * for each supported filter, without requiring a real database.
 */
@ExtendWith(MockitoExtension.class)
class ProductSpecificationTest {

    @Mock
    private Root<Product> root;
    @Mock
    private CriteriaQuery<?> query;
    @Mock
    private CriteriaBuilder cb;
    @Mock
    private Path<Object> path;
    @Mock
    private Expression<String> lowerExpression;
    @Mock
    private Predicate predicate;

    @SuppressWarnings("unchecked")
    private void stubCommonPathAccess() {
        lenient().when(root.<Object>get(anyString())).thenReturn(path);
        lenient().when(cb.equal(any(), any())).thenReturn(predicate);
        lenient().when(cb.and(any(Predicate[].class))).thenReturn(predicate);
        lenient().when(cb.or(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);
    }

    @Test
    void fromRequest_appliesKeywordFilter_whenKeywordProvided() {
        stubCommonPathAccess();
        when(cb.lower(any())).thenReturn(lowerExpression);
        when(cb.like(any(), anyString())).thenReturn(predicate);

        ProductSearchRequest request = new ProductSearchRequest();
        request.setKeyword("pizza");

        Specification<Product> spec = ProductSpecification.fromRequest(request);
        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb, atLeastOnce()).like(any(), anyString());
        verify(cb).or(any(Predicate.class), any(Predicate.class));
    }

    @Test
    void fromRequest_appliesVegAndPriceRangeFilters_whenProvided() {
        stubCommonPathAccess();
        when(cb.greaterThanOrEqualTo(any(), any(Integer.class))).thenReturn(predicate);
        when(cb.lessThanOrEqualTo(any(), any(Integer.class))).thenReturn(predicate);

        ProductSearchRequest request = new ProductSearchRequest();
        request.setIsVeg(true);
        request.setMinPrice(100);
        request.setMaxPrice(500);

        Specification<Product> spec = ProductSpecification.fromRequest(request);
        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).equal(path, true);
        verify(cb).greaterThanOrEqualTo(any(), eq(100));
        verify(cb).lessThanOrEqualTo(any(), eq(500));
    }

    @Test
    void fromRequest_appliesNoFilters_whenRequestEmpty() {
        stubCommonPathAccess();

        Specification<Product> spec = ProductSpecification.fromRequest(new ProductSearchRequest());
        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).and(new Predicate[0]);
    }
}
