package com.example.user_service.integration;

import com.example.user_service.dto.UserDTO;
import com.example.user_service.model.User;
import com.example.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerIntegrationTest extends AbstractIntegrationTest {
 
    @Autowired
    private UserRepository userRepository;
 
    @Autowired
    private CacheManager cacheManager;
 
    private UserDTO baseUserDTO;
 
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        baseUserDTO = UserDTO.builder()
                .name("Alice")
                .surname("Rossi")
                .email("Alice.rossi@example.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    void createUser_validPayload_returns201() throws Exception {
        
        mockMvc.perform(post("/api/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseUserDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(baseUserDTO.getEmail()))
                .andExpect(jsonPath("$.id").isNumber());
    }
 
    @Test
    void createUser_duplicateEmail_reactivatesExistingUser() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseUserDTO)))
                .andExpect(status().isCreated());
 
        User existing = userRepository.findUserByEmail(baseUserDTO.getEmail());
        existing.setActive(false);
        userRepository.save(existing);
 
        mockMvc.perform(post("/api/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseUserDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(baseUserDTO.getEmail()));
 
        User reactivated = userRepository.findUserByEmail(baseUserDTO.getEmail());
        assertTrue(reactivated.isActive());
    }
 
    @Test
    void createUser_missingRequiredFields_returns400() throws Exception {
        UserDTO invalid = UserDTO.builder().build(); 
        mockMvc.perform(post("/api/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }
 
    @Test
    void createUser_noInternalSecret_returns403() throws Exception {
        
        MockMvc cleanMockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        cleanMockMvc.perform(post("/api/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseUserDTO)))
                .andExpect(status().isForbidden());
    }
 
    @Test
    void getUserById_asAdmin_existingUser_returns200() throws Exception {
        Long id = createUserInDb(baseUserDTO);
 
        mockMvc.perform(get("/api/users/{id}", id)
                        .with(internalAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.email").value(baseUserDTO.getEmail()));
    }
 
    @Test
    void getUserById_asOwner_returns200() throws Exception {
        Long id = createUserInDb(baseUserDTO);
 
        mockMvc.perform(get("/api/users/{id}", id)
                        .with(userAuth(id)))
                .andExpect(status().isOk());
    }

    @Test
    void getUserById_nonExistentId_returns404() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 99999L)
                        .with(internalAuth()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserById_isServedFromCache_onSecondCall() throws Exception {
        Long id = createUserInDb(baseUserDTO);
 
        mockMvc.perform(get("/api/users/{id}", id).with(internalAuth()))
                .andExpect(status().isOk());
 
        userRepository.findById(id).ifPresent(u -> {
            u.setName("CacheBypassName");
            userRepository.save(u);
        });
 
        mockMvc.perform(get("/api/users/{id}", id).with(internalAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(baseUserDTO.getName()));
    }
 
    @Test
    void getAllUsers_asAdmin_returnsPaginatedResults() throws Exception {
        createUserInDb(baseUserDTO);
        createUserInDb(UserDTO.builder()
                .name("Jane").surname("Smith").email("jane@example.com")
                .birthDate(LocalDate.of(1995, 5, 5)).build());
 
        mockMvc.perform(get("/api/users")
                        .with(internalAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(2)));
    }
 
    @Test
    void getAllUsers_filterByFirstName_returnsFiltered() throws Exception {
        createUserInDb(baseUserDTO);
        createUserInDb(UserDTO.builder()
                .name("Alice").surname("Wonder").email("alice@example.com")
                .birthDate(LocalDate.of(2000, 3, 3)).build());
 
        mockMvc.perform(get("/api/users")
                        .param("firstName", "Alice")
                        .with(internalAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Alice"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }
 
    @Test
    void updateUser_asAdmin_returns200() throws Exception {
        Long id = createUserInDb(baseUserDTO);
        UserDTO update = UserDTO.builder()
                .name("Updated").surname("Name")
                .email("updated@example.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();
 
        mockMvc.perform(put("/api/users/{id}", id)
                        .with(internalAuth())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }
 
    @Test
    void updateUser_asOwner_returns200() throws Exception {
        Long id = createUserInDb(baseUserDTO);
        UserDTO update = UserDTO.builder()
                .name("Self").surname("Update")
                .email("self@example.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();
 
        mockMvc.perform(put("/api/users/{id}", id)
                        .with(userAuth(id))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk());
    }
 
    @Test
    void updateUser_nonExistentId_returns500() throws Exception {
        mockMvc.perform(put("/api/users/{id}", 99999L)
                        .with(internalAuth())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseUserDTO)))
                .andExpect(status().isInternalServerError());
    }
 
    @Test
    void updateUser_invalidatesAndUpdatesCacheEntry() throws Exception {
        Long id = createUserInDb(baseUserDTO);
 
        mockMvc.perform(get("/api/users/{id}", id).with(internalAuth()))
                .andExpect(status().isOk());
 
        UserDTO update = UserDTO.builder()
                .name("Fresh").surname("Cache")
                .email("fresh@example.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();
 
        mockMvc.perform(put("/api/users/{id}", id)
                        .with(internalAuth())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk());
 
        mockMvc.perform(get("/api/users/{id}", id).with(internalAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Fresh"));
    }
 
    @Test
    void deleteUser_asAdmin_returns204() throws Exception {
        Long id = createUserInDb(baseUserDTO);
 
        mockMvc.perform(delete("/api/users/{id}", id)
                        .with(internalAuth()))
                .andExpect(status().isNoContent());
 
        User user = userRepository.findById(id).orElseThrow();
        assertFalse(user.isActive());
    }
 
 
    @Test
    void deleteUser_evictsCacheEntry() throws Exception {
        Long id = createUserInDb(baseUserDTO);
 
        mockMvc.perform(get("/api/users/{id}", id).with(internalAuth()))
                .andExpect(status().isOk());
 
        mockMvc.perform(delete("/api/users/{id}", id).with(internalAuth()))
                .andExpect(status().isNoContent());
 
        mockMvc.perform(get("/api/users/{id}", id).with(internalAuth()))
                .andExpect(status().isNotFound());
    }
 
    @Test
    void deleteUser_nonExistentId_returns404() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 99999L)
                        .with(internalAuth()))
                .andExpect(status().isNotFound());
    }
 
    @Test
    void setUserStatus_deactivate_returns204() throws Exception {
        Long id = createUserInDb(baseUserDTO);
 
        mockMvc.perform(patch("/api/users/{id}/status", id)
                        .param("active", "false")
                        .with(internalAuth()))
                .andExpect(status().isNoContent());
 
        assertFalse(userRepository.findById(id).orElseThrow().isActive());
    }
 
    @Test
    void setUserStatus_activate_returns204() throws Exception {
        Long id = createUserInDb(baseUserDTO);
        setUserActive(id, false);
 
        mockMvc.perform(patch("/api/users/{id}/status", id)
                        .param("active", "true")
                        .with(internalAuth()))
                .andExpect(status().isNoContent());
 
        assertTrue(userRepository.findById(id).orElseThrow().isActive());
    }

    private Long createUserInDb(UserDTO dto) throws Exception {
        String response = mockMvc.perform(post("/api/users")
                        .with(internalAuth())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
 
        return objectMapper.readTree(response).get("id").asLong();
    }
 
    private void setUserActive(Long id, boolean active) {
        userRepository.findById(id).ifPresent(u -> {
            u.setActive(active);
            userRepository.save(u);
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