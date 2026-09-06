package com.clientos.backend.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Module 17: proves the filter's actual authentication decision directly
// -- no Spring context, no HTTP layer, no database -- which is more
// precise and far more reliable than routing this through the full
// security auto-configuration chain in a @WebMvcTest.
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        // Built here, not as a field initializer -- field initializers
        // run before MockitoExtension injects the @Mock fields, which
        // would leave jwtService/userDetailsService null inside filter.
        filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void requestWithNoAuthorizationHeaderIsNotAuthenticated() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response); // still proceeds -- SecurityConfig rejects it later
    }

    @Test
    void requestWithInvalidTokenIsNotAuthenticated() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer garbage-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtService.isValid("garbage-token")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void requestWithValidTokenIsAuthenticatedAsThatUser() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isValid("valid-token")).thenReturn(true);
        when(jwtService.extractEmail("valid-token")).thenReturn("a@test.com");
        UserDetails userDetails = User.withUsername("a@test.com").password("hash").authorities("ROLE_EMPLOYEE").build();
        when(userDetailsService.loadUserByUsername("a@test.com")).thenReturn(userDetails);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("a@test.com");
    }
}
