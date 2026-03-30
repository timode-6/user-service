package com.example.user_service.service.userservice;

import com.example.user_service.dto.UserDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserService {
    
    UserDTO createUser(UserDTO userDto);

    Optional<UserDTO> getUserById(Long id);

    Page<UserDTO> getAllUsers(String firstName, String surname, Pageable pageable);

    @Transactional
    Optional<UserDTO> updateUser(Long id, UserDTO updateUserDto);
    
    @Transactional
    void deleteUser(Long id);

    void activateDeactivateUser(Long id, boolean active);

}
