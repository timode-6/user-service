package com.example.user_service.integration;

import com.example.user_service.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;

import static org.hamcrest.Matchers.hasSize;

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

class UserControllerIntegrationTest extends AbstractIntegrationTest {
    

    @Autowired
    private UserRepository userRepository;
    

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }


    private String userJson(String name, String surname, String email, String birthDate, boolean active) {
        return """
                {
                    "name": "%s",
                    "surname": "%s",
                    "email": "%s",
                    "birthDate": "%s",
                    "active": %s
                }
                """.formatted(name, surname, email, birthDate, active);
    }

    private String defaultUserJson() {
        return userJson("John", "Doe", "john.doe@example.com", "1990-01-15", true);
    }

    private Long createUserAndReturnId(String json) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("id").asLong();
    }


    @Nested
    @DisplayName("POST /api/users")
    class CreateUser {

        @Test
        @DisplayName("should create a new user and return 201")
        void shouldCreateUser() throws Exception {
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(defaultUserJson()))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.name").value("John"))
                    .andExpect(jsonPath("$.surname").value("Doe"))
                    .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                    .andExpect(jsonPath("$.active").value(true));

            assertThat(userRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("should reactivate an existing user with the same email")
        void shouldReactivateExistingUser() throws Exception {
            Long userId = createUserAndReturnId(defaultUserJson());
            mockMvc.perform(delete("/api/users/{id}", userId))
                    .andExpect(status().isNoContent());

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(defaultUserJson()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.active").value(true));

            assertThat(userRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("should return 400 when required fields are missing")
        void shouldReturn400WhenInvalid() throws Exception {
            String invalidJson = """
                    { "name": "" }
                    """;

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/users/{id}")
    class GetUserById {

        @Test
        @DisplayName("should return user when exists and is active")
        void shouldReturnUser() throws Exception {
            Long userId = createUserAndReturnId(defaultUserJson());

            mockMvc.perform(get("/api/users/{id}", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId))
                    .andExpect(jsonPath("$.name").value("John"))
                    .andExpect(jsonPath("$.surname").value("Doe")).andDo(result -> System.out.println(result.getResponse().getContentAsString()));
        }

        @Test
        @DisplayName("should return 404 when user does not exist")
        void shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(get("/api/users/{id}", 99999L))
                    .andExpect(status().isNotFound()).andDo(result -> System.out.println(result.getResponse().getContentAsString()));
        }

        @Test
        @DisplayName("should return 404 for a soft-deleted (inactive) user")
        void shouldReturn404ForInactiveUser() throws Exception {
            Long userId = createUserAndReturnId(defaultUserJson());

            mockMvc.perform(delete("/api/users/{id}", userId))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/users/{id}", userId))
                    .andExpect(status().isNotFound()).andDo(result -> System.out.println(result.getResponse().getContentAsString()));
        }
    }


    @Nested
    @DisplayName("PUT /api/users/{id}")
    class UpdateUser {

        @Test
        @DisplayName("should update user fields and return 200")
        void shouldUpdateUser() throws Exception {
            Long userId = createUserAndReturnId(defaultUserJson());

            String updatedJson = userJson("John", "Smith", "john.smith@example.com", "1990-01-15", true);

            mockMvc.perform(put("/api/users/{id}", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updatedJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId))
                    .andExpect(jsonPath("$.surname").value("Smith"))
                    .andExpect(jsonPath("$.email").value("john.smith@example.com"));

            mockMvc.perform(get("/api/users/{id}", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.surname").value("Smith"));
        }

        @Test
        @DisplayName("should return error when updating non-existent user")
        void shouldFailWhenUserNotFound() throws Exception {
            String json = defaultUserJson();

            mockMvc.perform(put("/api/users/{id}", 99999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().is5xxServerError());
        }
    }


    @Nested
    @DisplayName("DELETE /api/users/{id}")
    class DeleteUser {

        @Test
        @DisplayName("should soft-delete user and return 204")
        void shouldSoftDeleteUser() throws Exception {
            Long userId = createUserAndReturnId(defaultUserJson());

            mockMvc.perform(delete("/api/users/{id}", userId))
                    .andExpect(status().isNoContent());

            assertThat(userRepository.findById(userId)).isPresent();
            assertThat(userRepository.findById(userId).get().isActive()).isFalse();
        }
    }
}