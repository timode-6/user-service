package com.example.user_service.unit;

import com.example.user_service.dto.PaymentCardDTO;
import com.example.user_service.dto.UserDTO;
import com.example.user_service.exception.PaymentCardNotFoundException;
import com.example.user_service.exception.UserNotFoundException;
import com.example.user_service.model.PaymentCard;
import com.example.user_service.model.User;
import com.example.user_service.repository.PaymentCardRepository;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.service.paymentcardservice.*;
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

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentCardServiceImpl Tests")
class PaymentCardServiceImplTest {

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
    @DisplayName("createPaymentCard()")
    class CreatePaymentCardTests {

        @Test
        @DisplayName("Should return PaymentCardDTO when user exists")
        void shouldReturnPaymentCardDTO_whenUserExists() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(paymentCardRepository.save(any(PaymentCard.class))).thenReturn(paymentCard);

            PaymentCardDTO result = paymentCardService.createPaymentCard(1L, paymentCardDTO);

            assertThat(result).isNotNull();
            assertThat(result.getNumber()).isEqualTo(paymentCardDTO.getNumber());
            assertThat(result.getHolder()).isEqualTo(paymentCardDTO.getHolder());
            verify(userRepository, times(1)).findById(1L);
            verify(paymentCardRepository, times(1)).save(any(PaymentCard.class));
        }

        @ParameterizedTest
        @ValueSource(longs = {99L})
        @DisplayName("Should throw RuntimeException when user is not found")
        void shouldThrowException_whenUserNotFound(Long id) {
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentCardService.createPaymentCard(id, paymentCardDTO))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("User not found with ID: " + id);

            verify(paymentCardRepository, never()).save(any(PaymentCard.class));
        }
    }


    @Nested
    @DisplayName("getPaymentCardById()")
    class GetPaymentCardByIdTests {

        @Test
        @DisplayName("Should return populated Optional when card exists")
        void shouldReturnPaymentCardDTO_whenCardExists() {
            when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(paymentCard));

            Optional<PaymentCardDTO> result = paymentCardService.getPaymentCardById(1L, 1L);

            assertThat(result).isPresent();
            assertThat(result.get().getNumber()).isEqualTo(paymentCard.getNumber());
            verify(paymentCardRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should return empty Optional when card is not found")
        void shouldReturnEmpty_whenCardNotFound() {
            when(paymentCardRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<PaymentCardDTO> result = paymentCardService.getPaymentCardById(1L, 99L);

            assertThat(result).isEmpty();
            verify(paymentCardRepository, times(1)).findById(99L);
        }
    }



    @Nested
    @DisplayName("getAllCardsByUserId()")
    class GetAllCardsByUserIdTests {

        @Test
        @DisplayName("Should return a set of cards when user has cards")
        void shouldReturnSetOfCards_whenUserHasCards() {
            when(paymentCardRepository.findByUserId(1L)).thenReturn(List.of(paymentCard));

            Set<PaymentCardDTO> result = paymentCardService.getAllCardsByUserId(1L);

            assertThat(result).hasSize(1);
            verify(paymentCardRepository, times(1)).findByUserId(1L);
        }

        @Test
        @DisplayName("Should return empty set when user has no cards")
        void shouldReturnEmptySet_whenUserHasNoCards() {
            when(paymentCardRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

            Set<PaymentCardDTO> result = paymentCardService.getAllCardsByUserId(1L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("deletePaymentCard()")
    class DeletePaymentCardTests {

        @Test
        @DisplayName("Should call deleteById once when deleting a card")
        void shouldCallDeleteById_whenDeletingCard() {
            when(paymentCardRepository.findById(paymentCard.getId())).thenReturn(Optional.of(paymentCard));
        
            paymentCardService.deletePaymentCard(user.getId(), 1L);

            verify(paymentCardRepository, times(1)).save(paymentCard);
        }
    }


    @Nested
    @DisplayName("updatePaymentCard()")
    class UpdatePaymentCardTests {

        @Test
        @DisplayName("Should return updated PaymentCardDTO when card exists")
        void shouldReturnUpdatedCard_whenCardExists() {
            PaymentCardDTO updatedDTO = new PaymentCardDTO();
            updatedDTO.setNumber("9876543210987654");
            updatedDTO.setHolder("Jane Doe");
            updatedDTO.setExpirationDate(LocalDate.of(2027, 6, 30));
            updatedDTO.setActive(false);

            PaymentCard updatedCard = new PaymentCard();
            updatedCard.setId(1L);
            updatedCard.setNumber("9876543210987654");
            updatedCard.setHolder("Jane Doe");
            updatedCard.setExpirationDate(LocalDate.of(2027, 6, 30));
            updatedCard.setActive(false);

            when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(paymentCard));
            when(paymentCardRepository.save(any(PaymentCard.class))).thenReturn(updatedCard);

            PaymentCardDTO result = paymentCardService.updatePaymentCard(1L, updatedDTO);

            assertThat(result).isNotNull();
            assertThat(result.getNumber()).isEqualTo("9876543210987654");
            assertThat(result.getHolder()).isEqualTo("Jane Doe");
            verify(paymentCardRepository, times(1)).findById(1L);
            verify(paymentCardRepository, times(1)).save(any(PaymentCard.class));
        }

        @ParameterizedTest
        @ValueSource(longs = 99L)
        @DisplayName("Should throw RuntimeException when card is not found")
        void shouldThrowException_whenCardNotFound(Long id) {
            when(paymentCardRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentCardService.updatePaymentCard(id, paymentCardDTO))
                    .isInstanceOf(PaymentCardNotFoundException.class)
                    .hasMessage("Payment card not found: " + id);

            verify(paymentCardRepository, never()).save(any(PaymentCard.class));
        }
    }


    @Nested
    @DisplayName("activateDeactivateCard()")
    class ActivateDeactivateCardTests {

        @Test
        @DisplayName("Should activate card when active is true")
        void shouldActivateCard_whenActiveIsTrue() {
            paymentCard.setActive(false);
            when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(paymentCard));
            when(paymentCardRepository.save(paymentCard)).thenReturn(paymentCard);

            paymentCardService.activateDeactivateCard(1L, 1L, true);

            assertThat(paymentCard.isActive()).isTrue();
            verify(paymentCardRepository).save(paymentCard);
        }

        @Test
        @DisplayName("Should deactivate card when active is false")
        void shouldDeactivateCard_whenActiveIsFalse() {
            paymentCard.setActive(true);
            when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(paymentCard));
            when(paymentCardRepository.save(paymentCard)).thenReturn(paymentCard);

            paymentCardService.activateDeactivateCard(1L, 1L, false);

            assertThat(paymentCard.isActive()).isFalse();
            verify(paymentCardRepository).save(paymentCard);
        }

        @ParameterizedTest
        @ValueSource(longs = {99L})
        @DisplayName("Should throw RuntimeException when card is not found")
        void shouldThrowException_whenCardNotFound(Long id) {
            when(paymentCardRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentCardService.activateDeactivateCard(1L, id, true))
                    .isInstanceOf(PaymentCardNotFoundException.class)
                    .hasMessage("Payment card not found: " + id);

            verify(paymentCardRepository, never()).save(any(PaymentCard.class));
        }
    }
}