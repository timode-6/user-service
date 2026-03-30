package com.example.user_service.integration;

import com.example.user_service.repository.PaymentCardRepository;
import com.example.user_service.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PaymentCardControllerIntegrationTest extends AbstractIntegrationTest {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @BeforeEach
    void cleanDatabase() {
        paymentCardRepository.deleteAll();
        userRepository.deleteAll();
    }


    private Long createTestUser() throws Exception {
        String userJson = """
                {
                    "name": "Alice",
                    "surname": "Wonder",
                    "email": "alice@example.com",
                    "birthDate": "1988-03-22",
                    "active": true
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }

    private String cardJson(String number, String holder, String expirationDate, boolean active) {
        return """
                {
                    "number": "%s",
                    "holder": "%s",
                    "expirationDate": "%s",
                    "active": %s
                }
                """.formatted(number, holder, expirationDate, active);
    }

    private String defaultCardJson() {
        return cardJson("4111111111111111", "Alice Wonder", "2027-12-31", true);
    }

    private Long createCardAndReturnId(Long userId, String json) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/card/{userId}/cards", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("id").asLong();
    }


    @Nested
    @DisplayName("POST /api/card/{userId}/cards")
    class CreatePaymentCard {

        @Test
        @DisplayName("should create a card for an active user and return 201")
        void shouldCreateCard() throws Exception {
            Long userId = createTestUser();

            mockMvc.perform(post("/api/card/{userId}/cards", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(defaultCardJson()))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.number").value("4111111111111111"))
                    .andExpect(jsonPath("$.holder").value("Alice Wonder"));

            assertThat(paymentCardRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("should fail when user does not exist")
        void shouldFailForNonExistentUser() throws Exception {
            mockMvc.perform(post("/api/card/{userId}/cards", 99999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(defaultCardJson()))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("should fail when user is inactive (soft-deleted)")
        void shouldFailForInactiveUser() throws Exception {
            Long userId = createTestUser();

            mockMvc.perform(delete("/api/users/{id}", userId))
                    .andExpect(status().isNoContent());

            mockMvc.perform(post("/api/card/{userId}/cards", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(defaultCardJson()))
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("should allow multiple cards for the same user")
        void shouldAllowMultipleCards() throws Exception {
            Long userId = createTestUser();

            createCardAndReturnId(userId, cardJson("4111111111111111", "Alice Wonder", "2027-12-31", true));
            createCardAndReturnId(userId, cardJson("5500000000000004", "Alice Wonder", "2028-06-30", true));

            assertThat(paymentCardRepository.count()).isEqualTo(2);
        }
    }


    @Nested
    @DisplayName("GET /api/card/{userId}/cards/{cardId}")
    class GetPaymentCard {

        @Test
        @DisplayName("should return the card when it exists")
        void shouldReturnCard() throws Exception {
            Long userId = createTestUser();
            Long cardId = createCardAndReturnId(userId, defaultCardJson());

            mockMvc.perform(get("/api/card/{userId}/cards/{cardId}", userId, cardId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(cardId))
                    .andExpect(jsonPath("$.number").value("4111111111111111"))
                    .andExpect(jsonPath("$.holder").value("Alice Wonder"));
        }

        @Test
        @DisplayName("should return 404 when card does not exist")
        void shouldReturn404ForMissingCard() throws Exception {
            Long userId = createTestUser();

            mockMvc.perform(get("/api/card/{userId}/cards/{cardId}", userId, 99999L))
                    .andExpect(status().isNotFound());
        }
    }



    @Nested
    @DisplayName("PUT /api/card/cards/{id}")
    class UpdatePaymentCard {

        @Test
        @DisplayName("should update card fields and return 200")
        void shouldUpdateCard() throws Exception {
            Long userId = createTestUser();
            Long cardId = createCardAndReturnId(userId, defaultCardJson());

            String updatedJson = cardJson("4222222222222222", "Alice W. Smith", "2029-01-31", true);

            mockMvc.perform(put("/api/card/cards/{id}", cardId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updatedJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.number").value("4222222222222222"))
                    .andExpect(jsonPath("$.holder").value("Alice W. Smith"))
                    .andExpect(jsonPath("$.expirationDate").exists());

            mockMvc.perform(get("/api/card/{userId}/cards/{cardId}", userId, cardId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.number").value("4222222222222222"));
        }

        @Test
        @DisplayName("should return error when card does not exist")
        void shouldFailWhenCardNotFound() throws Exception {
            mockMvc.perform(put("/api/card/cards/{id}", 99999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(defaultCardJson()))
                    .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    @DisplayName("DELETE /api/card/cards/{id}")
    class DeletePaymentCard {

        @Test
        @DisplayName("should soft-delete card and return 204")
        void shouldSoftDeleteCard() throws Exception {
            Long userId = createTestUser();
            Long cardId = createCardAndReturnId(userId, defaultCardJson());

            mockMvc.perform(delete("/api/card/cards/{id}", cardId))
                    .andExpect(status().isNoContent());

            // Row still in DB but inactive
            assertThat(paymentCardRepository.findById(cardId)).isPresent();
            assertThat(paymentCardRepository.findById(cardId).get().isActive()).isFalse();
        }
    }
}