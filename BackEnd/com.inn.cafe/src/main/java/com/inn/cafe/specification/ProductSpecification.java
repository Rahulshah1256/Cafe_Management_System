package com.inn.cafe.specification;

import com.inn.cafe.POJO.Product;
import com.inn.cafe.dto.ProductSearchRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a dynamic {@link Specification} for {@link Product} search/filtering based on
 * whichever criteria are present on {@link ProductSearchRequest}. Keeping this isolated from
 * the service layer follows SRP and makes each filter independently unit-testable.
 */
public final class ProductSpecification {

    private ProductSpecification() {
    }

    public static Specification<Product> fromRequest(ProductSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
                String likePattern = "%" + request.getKeyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), likePattern),
                        cb.like(cb.lower(root.get("description")), likePattern)
                ));
            }

            if (request.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), request.getCategoryId()));
            }

            if (request.getIsVeg() != null) {
                predicates.add(cb.equal(root.get("isVeg"), request.getIsVeg()));
            }

            if (request.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), request.getMinPrice()));
            }

            if (request.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), request.getMaxPrice()));
            }

            if (request.getMinRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rating"), request.getMinRating()));
            }

            if (request.getSpicyLevel() != null && !request.getSpicyLevel().isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("spicyLevel")), request.getSpicyLevel().toUpperCase()));
            }

            if (request.getBestSeller() != null) {
                predicates.add(cb.equal(root.get("bestSeller"), request.getBestSeller()));
            }

            if (request.getNewArrival() != null) {
                predicates.add(cb.equal(root.get("newArrival"), request.getNewArrival()));
            }

            if (request.getStatus() != null && !request.getStatus().isBlank()) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
