package com.example.user_service.spec;

import com.example.user_service.model.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {
    public static Specification<User> hasFirstName(String firstName){
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("name"), firstName);
        };
    }

    public static Specification<User> hasSurname(String surname){
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("surname"), surname);
        };
    }

    public static Specification<User> isActive(){
         return (root, query, builder) ->
            builder.equal(root.get("active"), true);
    }
}
