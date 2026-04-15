package com.example.user_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
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
    protected MockMvc mockMvc;

    protected ObjectMapper objectMapper = new ObjectMapper();

    @Autowired(required = false)
    protected CacheManager cacheManager; 

    @BeforeEach
    void evictAllCaches() {
        if (cacheManager != null) {
            cacheManager.getCacheNames()
                    .forEach(name -> {
                        var cache = cacheManager.getCache(name);
                        if (cache != null) cache.clear();
                    });
        }
    }
    @BeforeEach
    void checkContainerRunning() {
        assertTrue(
            postgres.isRunning(), 
            "PostgreSQL container must be running!"
        );
    }
}