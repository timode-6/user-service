package com.example.user_service.service.userservice;

import com.example.user_service.model.User;
import com.example.user_service.dto.UserDTO;
import com.example.user_service.exception.UserNotFoundException;
import com.example.user_service.exception.UserUpdateFailedException;
import com.example.user_service.mapper.UserMapper;
import com.example.user_service.repository.*;
import com.example.user_service.spec.UserSpecification;

import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService{
    
    private UserRepository userRepository;

    public static final String USER_CACHE = "users";

    @Override
    @CachePut(value = USER_CACHE, key = "#result.id")
    public UserDTO createUser(UserDTO userDTO){
        return Optional.ofNullable(userRepository.findUserByEmail(userDTO.getEmail()))
        .map(existingUser -> {
            existingUser.setActive(true);
            userRepository.save(existingUser); 
            return UserMapper.INSTANCE.userToUserDTO(existingUser);
        })
        .orElseGet(() -> {
            User user = UserMapper.INSTANCE.userDtoToUser(userDTO);
            user.setActive(true); 
            return UserMapper.INSTANCE.userToUserDTO(userRepository.save(user));
        });
    }

    @Override
    @Cacheable(value = USER_CACHE, key = "#id", unless = "#result == null || !#result.isPresent()")
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id).filter(User::isActive).map(UserMapper.INSTANCE::userToUserDTO);
    }


    @Override
    @Cacheable(value = USER_CACHE,
           key = "T(java.util.Objects).hash(#firstName, #surname, #pageable)",
           unless = "#result == null || #result.isEmpty()")
    public Page<UserDTO> getAllUsers(String firstName, String surname, Pageable pageable) {
        Specification<User> spec = UserSpecification.isActive();
        if (firstName != null) {
            spec = spec.and(UserSpecification.hasFirstName(firstName));
        }
        if (surname != null) {
            spec = spec.and(UserSpecification.hasSurname(surname));
        }
        Page<User> usersPage = userRepository.findAll(spec, pageable);
        return usersPage.map(UserMapper.INSTANCE::userToUserDTO);
    }

 
    @Override
    @Transactional
    @CachePut(value = USER_CACHE, key = "#id")
    public Optional<UserDTO> updateUser(Long id, UserDTO updatedUserDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserUpdateFailedException(id));
        user.setName(updatedUserDTO.getName());
        user.setSurname(updatedUserDTO.getSurname());
        user.setBirthDate(updatedUserDTO.getBirthDate());
        user.setEmail(updatedUserDTO.getEmail());
        user.setActive(updatedUserDTO.isActive());
        return Optional.of(UserMapper.INSTANCE.userToUserDTO(userRepository.save(user)));
    }

    @Override
    @Transactional
    @CacheEvict(value = USER_CACHE, key = "#id")
    public void deleteUser(Long id) {
         User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
        user.setActive(false);  
        userRepository.save(user);
    }
   
    @Override
    public void activateDeactivateUser(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setActive(active);
        userRepository.save(user);
    }

    
}
