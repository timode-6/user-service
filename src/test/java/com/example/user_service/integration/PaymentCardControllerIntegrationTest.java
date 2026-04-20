package com.example.user_service.integration;

import com.example.user_service.repository.PaymentCardRepository;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.service.paymentcardservice.PaymentCardServiceImpl;
import com.example.user_service.dto.PaymentCardDTO;
import com.example.user_service.model.PaymentCard;
import com.example.user_service.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class PaymentCardControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    private Long activeUserId;
    private Long inactiveUserId;
    private PaymentCardDTO baseCardDTO;

    @BeforeEach
    void setUp() {
        paymentCardRepository.deleteAll();
        userRepository.deleteAll();

        User active = userRepository.save(User.builder()
                .name("Active").surname("User")
                .email("active@example.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .active(true).build());
        activeUserId = active.getId();

        User inactive = userRepository.save(User.builder()
                .name("Inactive").surname("User")
                .email("inactive@example.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .active(false).build());
        inactiveUserId = inactive.getId();

        baseCardDTO = PaymentCardDTO.builder()
                .number("4111111111111111")
                .holder("ACTIVE USER")
                .expirationDate(LocalDate.of(2027, 12, 1))
                .build();
    }

    @Test
    void createCard_asAdmin_validUser_returns201() throws Exception {
        mockMvc.perform(post("/api/card/{userId}/cards", activeUserId)
                        .with(internalAuth())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseCardDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.number").value(baseCardDTO.getNumber()));
    }

    @Test
    void createCard_asOwner_returns201() throws Exception {
        mockMvc.perform(post("/api/card/{userId}/cards", activeUserId)
                        .with(userAuth(activeUserId))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseCardDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    void createCard_inactiveUser_returns500() throws Exception {
        mockMvc.perform(post("/api/card/{userId}/cards", inactiveUserId)
                        .with(internalAuth())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseCardDTO)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void createCard_nonExistentUser_returns404() throws Exception {
        mockMvc.perform(post("/api/card/{userId}/cards", 99999L)
                        .with(internalAuth())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseCardDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCard_invalidPayload_returns400() throws Exception {
        PaymentCardDTO invalid = PaymentCardDTO.builder().build();
        mockMvc.perform(post("/api/card/{userId}/cards", activeUserId)
                        .with(internalAuth())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCard_isAddedToCache() throws Exception {
        String response = mockMvc.perform(post("/api/card/{userId}/cards", activeUserId)
                        .with(internalAuth())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseCardDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long cardId = objectMapper.readTree(response).get("id").asLong();

        Cache cardCache = cacheManager.getCache(PaymentCardServiceImpl.CARD_CACHE);
        assertNotNull(cardCache);
        assertNotNull(cardCache.get(cardId));
    }

    @Test
    void getCardById_asAdmin_existingCard_returns200() throws Exception {
        Long cardId = createCard(activeUserId, baseCardDTO);

        mockMvc.perform(get("/api/card/{userId}/cards/{cardId}", activeUserId, cardId)
                        .with(internalAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId));
    }

    @Test
    void getCardById_asOwner_returns200() throws Exception {
        Long cardId = createCard(activeUserId, baseCardDTO);

        mockMvc.perform(get("/api/card/{userId}/cards/{cardId}", activeUserId, cardId)
                        .with(userAuth(activeUserId)))
                .andExpect(status().isOk());
    }

    void getCardById_nonExistentCard_returns404() throws Exception {
        mockMvc.perform(get("/api/card/{userId}/cards/{cardId}", activeUserId, 99999L)
                        .with(internalAuth()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCardById_isServedFromCache() throws Exception {
        Long cardId = createCard(activeUserId, baseCardDTO);

        mockMvc.perform(get("/api/card/{userId}/cards/{cardId}", activeUserId, cardId)
                        .with(internalAuth()))
                .andExpect(status().isOk());

        paymentCardRepository.findById(cardId).ifPresent(c -> {
            c.setHolder("DB BYPASS HOLDER");
            paymentCardRepository.save(c);
        });

        mockMvc.perform(get("/api/card/{userId}/cards/{cardId}", activeUserId, cardId)
                        .with(internalAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holder").value(baseCardDTO.getHolder()));
    }

    @Test
    void getAllCardsByUserId_asAdmin_returnsAllCards() throws Exception {
        createCard(activeUserId, baseCardDTO);
        createCard(activeUserId, PaymentCardDTO.builder()
                .number("5500005555555559").holder("ACTIVE USER")
                .expirationDate(LocalDate.of(2028, 6, 1)).build());

        mockMvc.perform(get("/api/card/{userId}/cards", activeUserId)
                        .with(internalAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAllCardsByUserId_noCards_returnsEmptySet() throws Exception {
        mockMvc.perform(get("/api/card/{userId}/cards", activeUserId)
                        .with(internalAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deleteCard_asAdmin_returns204() throws Exception {
        Long cardId = createCard(activeUserId, baseCardDTO);

        mockMvc.perform(delete("/api/card/{userId}/cards/{cardId}", activeUserId, cardId)
                        .with(internalAuth()))
                .andExpect(status().isNoContent());

        PaymentCard card = paymentCardRepository.findById(cardId).orElseThrow();
        assertFalse(card.isActive());
    }

    @Test
    void deleteCard_asOwner_returns204() throws Exception {
        Long cardId = createCard(activeUserId, baseCardDTO);

        mockMvc.perform(delete("/api/card/{userId}/cards/{cardId}", activeUserId, cardId)
                        .with(userAuth(activeUserId)))
                .andExpect(status().isNoContent());
    }

    void deleteCard_cardBelongsToDifferentUser_returns403or404() throws Exception {
        User other = userRepository.save(User.builder()
                .name("Other").surname("User").email("other2@example.com")
                .birthDate(LocalDate.of(1990, 1, 1)).active(true).build());
        Long otherCardId = createCard(other.getId(), baseCardDTO);

        mockMvc.perform(delete("/api/card/{userId}/cards/{cardId}", activeUserId, otherCardId)
                        .with(internalAuth()))
                .andExpect(status().is4xxClientError()); 
    }

    @Test
    void deleteCard_evictsCacheEntry() throws Exception {
        Long cardId = createCard(activeUserId, baseCardDTO);

        mockMvc.perform(get("/api/card/{userId}/cards/{cardId}", activeUserId, cardId)
                        .with(internalAuth()))
                .andExpect(status().isOk());

        Cache cardCache = cacheManager.getCache(PaymentCardServiceImpl.CARD_CACHE);
        assertNotNull(cardCache);
        assertNotNull(cardCache.get(cardId));

        mockMvc.perform(delete("/api/card/{userId}/cards/{cardId}", activeUserId, cardId)
                        .with(internalAuth()))
                .andExpect(status().isNoContent());

        assertNull(cardCache.get(cardId));
    }

    @Test
    void deleteCard_nonExistentCard_returns404() throws Exception {
        mockMvc.perform(delete("/api/card/{userId}/cards/{cardId}", activeUserId, 99999L)
                        .with(internalAuth()))
                .andExpect(status().isNotFound());
    }


    @Test
    void setCardStatus_asAdmin_deactivate_returns204() throws Exception {
        Long cardId = createCard(activeUserId, baseCardDTO);

        mockMvc.perform(patch("/api/card/{userId}/cards/{cardId}/status", activeUserId, cardId)
                        .param("active", "false")
                        .with(internalAuth()))
                .andExpect(status().isNoContent());

        assertFalse(paymentCardRepository.findById(cardId).orElseThrow().isActive());
    }

    @Test
    void setCardStatus_asAdmin_activate_returns204() throws Exception {
        Long cardId = createCard(activeUserId, baseCardDTO);
        deactivateCardInDb(cardId);

        mockMvc.perform(patch("/api/card/{userId}/cards/{cardId}/status", activeUserId, cardId)
                        .param("active", "true")
                        .with(internalAuth()))
                .andExpect(status().isNoContent());

        assertTrue(paymentCardRepository.findById(cardId).orElseThrow().isActive());
    }


    @Test
    void setCardStatus_cardBelongsToDifferentUser_returns4xx() throws Exception {
        User other = userRepository.save(User.builder()
                .name("Other").surname("User").email("other3@example.com")
                .birthDate(LocalDate.of(1990, 1, 1)).active(true).build());
        Long otherCardId = createCard(other.getId(), baseCardDTO);

        mockMvc.perform(patch("/api/card/{userId}/cards/{cardId}/status", activeUserId, otherCardId)
                        .param("active", "false")
                        .with(internalAuth()))
                .andExpect(status().is4xxClientError()); 
    }


    private Long createCard(Long userId, PaymentCardDTO dto) throws Exception {
        String response = mockMvc.perform(post("/api/card/{userId}/cards", userId)
                        .with(internalAuth())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    private void deactivateCardInDb(Long cardId) {
        paymentCardRepository.findById(cardId).ifPresent(c -> {
            c.setActive(false);
            paymentCardRepository.save(c);
        });
    }

    private static RequestPostProcessor userAuth(Long userId) {
        return request -> {
            request.addHeader("X-Internal-Secret", internal);
            request.addHeader("X-User-Id", userId.toString());
            request.addHeader("X-User-Role", "USER");
            return request;
        };
    }
}