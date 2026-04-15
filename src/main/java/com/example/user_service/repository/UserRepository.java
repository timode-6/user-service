package com.example.user_service.repository;

import com.example.user_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User>{
    
    User findByName(String name);

    List<User> findAllByActiveTrue();

    @Query(value = "SELECT * FROM users WHERE email = ?1", nativeQuery = true)
    User findUserByEmail(String email);

}