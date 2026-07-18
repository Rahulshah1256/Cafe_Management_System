package com.inn.cafe.restImpl;

import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.User;
import com.inn.cafe.dao.UserDao;
import com.inn.cafe.exception.ResourceNotFoundException;
import com.inn.cafe.rest.LoyaltyRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class LoyaltyRestImpl implements LoyaltyRest {

    @Autowired
    UserDao userDao;

    @Autowired
    JwtFilter jwtFilter;

    @Override
    public ResponseEntity<Map<String, Object>> getBalance() {
        User user = userDao.findByEmail(jwtFilter.getCurrentUsername());
        if (user == null) {
            throw new ResourceNotFoundException("Current user not found");
        }
        int points = user.getLoyaltyPoints() == null ? 0 : user.getLoyaltyPoints();
        return new ResponseEntity<>(Map.of(
                "points", points,
                "valuePerPoint", com.inn.cafe.constents.CafeConstants.LOYALTY_POINT_VALUE,
                "redeemableValue", points * com.inn.cafe.constents.CafeConstants.LOYALTY_POINT_VALUE
        ), HttpStatus.OK);
    }
}
