package com.example.user_service.integration;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:12-alpine")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      postgres::getJdbcUrl);
        registry.add("spring.datasource.username",  postgres::getUsername);
        registry.add("spring.datasource.password",  postgres::getPassword);
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> 5);
        registry.add("spring.datasource.hikari.connection-timeout", () -> 20000);
        registry.add("spring.datasource.hikari.initialization-fail-timeout", () -> 60000);
    }

    @ServiceConnection(name = "redis")
    static GenericContainer<?> redis =
        new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);

    static {
        postgres.start();
        redis.start();
    }

    @Autowired
    protected WebApplicationContext context; 

    protected MockMvc mockMvc;

    public static String internal = "1$MjQ0ODc4ZTNhOGM5N2ViNDIzYmEzNDJhN2VmMjMwMzE$GYaUg+8XVS1Y0WBhM2HdGJ0Wyjneyu19mQfd9OtutWQ";

    protected ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired(required = false)
    protected CacheManager cacheManager;
    
    @BeforeEach
    void setUpMockMvc() {
        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .defaultRequest(MockMvcRequestBuilders.get("/")
                    .with(csrf()) 
                    .header("X-Internal-Secret", internal)
                    .header("X-User-Id", "1")
                    .header("X-User-Role", "ADMIN"))
            .build();
        
    }

    protected static RequestPostProcessor internalAuth() {
        return request -> {
            request.addHeader("X-Internal-Secret", internal);
            request.addHeader("X-User-Id", "1");
            request.addHeader("X-User-Role", "ADMIN");
            return request;
        };
    }

    @BeforeEach
    void evictAllCaches() {
        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(name -> {
                var cache = cacheManager.getCache(name);
                if (cache != null) cache.clear();
            });
        }
    }

    @BeforeEach
    void checkContainerRunning() {
        assertTrue(postgres.isRunning(), "PostgreSQL container must be running!");
    }

    
}