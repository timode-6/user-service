package com.example.user_service.service;

import com.example.user_service.model.User;
import com.example.user_service.model.PaymentCard;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.repository.PaymentCardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setName("John");
        user.setSurname("Doe");
        user.setEmail("john.doe@example.com");
        user.setActive(true);
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setPaymentCards(new HashSet<>());
    }

    @Test
    void createUserTest() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        User createdUser = userService.createUser(user);

        assertNotNull(createdUser);
        assertEquals("John", createdUser.getName());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void createPaymentCardTest() {
        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setNumber("1234-5678-9012-3456");
        paymentCard.setUser(user);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(paymentCardRepository.save(any(PaymentCard.class))).thenReturn(paymentCard);

        PaymentCard createdCard = userService.createPaymentCard(1L, paymentCard);

        assertNotNull(createdCard);
        assertEquals("1234-5678-9012-3456", createdCard.getNumber());
        assertEquals(user, createdCard.getUser());
        verify(paymentCardRepository, times(1)).save(paymentCard);
    }

    @Test
    void getUserByIdTest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> foundUser = userService.getUserById(1L);

        assertTrue(foundUser.isPresent());
        assertEquals("John", foundUser.get().getName());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void updateUserTest() {
        User updatedUser = new User();
        updatedUser.setName("Jane");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.updateUser(1L, updatedUser);

        assertEquals("Jane", result.getName());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void activateDeactivateUserTest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.activateDeactivateUser(1L, false);

        assertFalse(user.isActive());
        verify(userRepository, times(1)).save(user);
    }

    
}