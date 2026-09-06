package com.clientos.backend.controller;

import com.clientos.backend.repository.UserRepository;
import com.clientos.backend.security.CustomUserDetailsService;
import com.clientos.backend.security.JwtAuthenticationFilter;
import com.clientos.backend.security.JwtService;
import com.clientos.backend.security.SecurityConfig;
import com.clientos.backend.service.ClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Module 17: exercises the REAL Spring Security filter chain (SecurityConfig,
// JwtAuthenticationFilter, JwtService) against real HTTP requests through
// MockMvc -- no live database needed, since @WebMvcTest loads only the web
// layer and ClientService/UserRepository are mocked.
//
// @EnableWebSecurity pulls in the standard Spring Security config
// infrastructure (AuthenticationConfiguration, ObjectPostProcessor, etc.)
// that SecurityConfig's authenticationManager() bean needs but a bare
// @WebMvcTest slice doesn't include on its own.
//
// The "valid token, wrong client -> 404" scenario is intentionally NOT
// tested here -- it's covered more precisely by two focused unit tests
// instead: JwtAuthenticationFilterTest (proves a valid token sets
// authentication) and ClientServiceTest (proves a non-owned client
// throws ClientNotFoundException). Together they give equivalent
// coverage without coupling one test to this project's full security
// auto-configuration wiring.
@WebMvcTest(ClientController.class)
@EnableWebSecurity
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, CustomUserDetailsService.class, JwtService.class})
@TestPropertySource(properties = {
        "jwt.secret=2VFFQ9JhgdMPSpYsBmjslZvhE+XzuPOkFDmcOSe+3kk=",
        "jwt.expiration-ms=3600000"
})
class ClientControllerAuthenticationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClientService clientService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void requestWithNoTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requestWithGarbageTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/clients").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }
}
