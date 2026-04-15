package com.example.user_service.unit;

import com.example.user_service.dto.PaymentCardDTO;
import com.example.user_service.dto.UserDTO;
import com.example.user_service.exception.UserNotFoundException;
import com.example.user_service.exception.UserUpdateFailedException;
import com.example.user_service.model.PaymentCard;
import com.example.user_service.model.User;
import com.example.user_service.repository.PaymentCardRepository;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.service.paymentcardservice.PaymentCardServiceImpl;
import com.example.user_service.service.userservice.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @InjectMocks
    private PaymentCardServiceImpl paymentCardService;

    private User user;
    private UserDTO userDTO;
    private PaymentCard paymentCard;
    private PaymentCardDTO paymentCardDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Alice");
        user.setSurname("Ross");
        user.setEmail("alice.ross@gmail.com");
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setActive(true);

        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setName("Alice");
        userDTO.setSurname("Ross");
        userDTO.setEmail("alice.ross@gmail.com");
        userDTO.setBirthDate(LocalDate.of(1990, 1, 1));
        userDTO.setActive(true);

        paymentCard = new PaymentCard();
        paymentCard.setId(1L);
        paymentCard.setNumber("1234567890123456");
        paymentCard.setHolder("Alice Ross");
        paymentCard.setExpirationDate(LocalDate.of(2025, 12, 31));
        paymentCard.setActive(true);
        paymentCard.setUser(user);

        paymentCardDTO = new PaymentCardDTO();
        paymentCardDTO.setId(1L);
        paymentCardDTO.setNumber("1234567890123456");
        paymentCardDTO.setHolder("Alice Ross");
        paymentCardDTO.setExpirationDate(LocalDate.of(2025, 12, 31));
        paymentCardDTO.setActive(true);
    }


    @Nested
    @DisplayName("createUser()")
    class CreateUserTests {

        @Test
        @DisplayName("Should return UserDTO when valid input is given")
        void shouldReturnUserDTO_whenValidInput() {
            when(userRepository.save(any(User.class))).thenReturn(user);

            UserDTO result = userService.createUser(userDTO);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(userDTO.getName());
            assertThat(result.getSurname()).isEqualTo(userDTO.getSurname());
            assertThat(result.getEmail()).isEqualTo(userDTO.getEmail());
            verify(userRepository, times(1)).save(any(User.class));
        }
    }


    @Nested
    @DisplayName("getUserById()")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should return populated Optional when user exists")
        void shouldReturnUserDTO_whenUserExists() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            Optional<UserDTO> result = userService.getUserById(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo(user.getName());
            verify(userRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should return empty Optional when user is not found")
        void shouldReturnEmpty_whenUserNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<UserDTO> result = userService.getUserById(99L);

            assertThat(result).isEmpty();
            verify(userRepository, times(1)).findById(99L);
        }
    }


    @Nested
    @DisplayName("getAllUsers()")
    class GetAllUsersTests {

        private Pageable pageable;

        @BeforeEach
        void init() {
            pageable = PageRequest.of(0, 10, Sort.by("name"));
        }

        @Test
        @DisplayName("Should return page of users with no filters applied")
        void shouldReturnPage_withNoFilters() {
            Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);
            when(userRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(userPage);

            Page<UserDTO> result = userService.getAllUsers(null, null, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(userRepository, times(1)).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Should return filtered page when firstName is provided")
        void shouldReturnFilteredPage_withFirstNameFilter() {
            Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);
            when(userRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(userPage);

            Page<UserDTO> result = userService.getAllUsers("John", null, pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return filtered page when surname is provided")
        void shouldReturnFilteredPage_withSurnameFilter() {
            Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);
            when(userRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(userPage);

            Page<UserDTO> result = userService.getAllUsers(null, "Doe", pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return filtered page when both firstName and surname are provided")
        void shouldReturnFilteredPage_withBothFilters() {
            Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);
            when(userRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(userPage);

            Page<UserDTO> result = userService.getAllUsers("John", "Doe", pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return empty page when no users match")
        void shouldReturnEmptyPage_whenNoUsersFound() {
            Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            when(userRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(emptyPage);

            Page<UserDTO> result = userService.getAllUsers(null, null, pageable);

            assertThat(result).isEmpty();
        }
    }


    @Nested
    @DisplayName("updateUser()")
    class UpdateUserTests {

        @Test
        @DisplayName("Should return updated UserDTO when user exists")
        void shouldReturnUpdatedUser_whenUserExists() {
            UserDTO updatedDTO = new UserDTO();
            updatedDTO.setName("Jane");
            updatedDTO.setSurname("Smith");
            updatedDTO.setEmail("jane.smith@example.com");
            updatedDTO.setBirthDate(LocalDate.of(1995, 5, 15));
            updatedDTO.setActive(false);

            User updatedUser = new User();
            updatedUser.setId(1L);
            updatedUser.setName("Jane");
            updatedUser.setSurname("Smith");
            updatedUser.setEmail("jane.smith@example.com");
            updatedUser.setBirthDate(LocalDate.of(1995, 5, 15));
            updatedUser.setActive(false);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(updatedUser);

            UserDTO result = userService.updateUser(1L, updatedDTO).get();

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Jane");
            assertThat(result.getSurname()).isEqualTo("Smith");
            verify(userRepository, times(1)).findById(1L);
            verify(userRepository, times(1)).save(any(User.class));
        }

        @ParameterizedTest
        @ValueSource(longs = {99L})
        @DisplayName("Should throw UserUpdateFailedException when user is not found")
        void shouldThrowException_whenUserNotFound(long userId) {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(userId, userDTO))
                    .isInstanceOf(UserUpdateFailedException.class)
                    .hasMessage("Failed to update: User with id " + userId + " not found");

            verify(userRepository, never()).save(any(User.class));
        }
    }


    @Nested
    @DisplayName("deleteUser()")
    class DeleteUserTests {

        @Test
        @DisplayName("Should call deleteById once when deleting a user")
        void shouldCallDeleteById_whenDeletingUser() {
           when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

            userService.deleteUser(user.getId());

            assertFalse(user.isActive());
            verify(userRepository, times(1)).save(user);

        }
    }


    @Nested
    @DisplayName("activateDeactivateUser()")
    class ActivateDeactivateUserTests {

        @Test
        @DisplayName("Should activate user when active is true")
        void shouldActivateUser_whenActiveIsTrue() {
            user.setActive(false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);

            userService.activateDeactivateUser(1L, true);

            assertThat(user.isActive()).isTrue();
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should deactivate user when active is false")
        void shouldDeactivateUser_whenActiveIsFalse() {
            user.setActive(true);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);

            userService.activateDeactivateUser(1L, false);

            assertThat(user.isActive()).isFalse();
            verify(userRepository).save(user);
        }

        @ParameterizedTest
        @ValueSource(longs = {99L})
        @DisplayName("Should throw RuntimeException when user is not found")
        void shouldThrowException_whenUserNotFound(Long id) {
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.activateDeactivateUser(id, true))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("User not found with ID: " + id);

            verify(userRepository, never()).save(any(User.class));
        }
    }
}