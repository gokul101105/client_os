package com.clientos.backend.controller;

import com.clientos.backend.dto.CreateAccountManagerRequest;
import com.clientos.backend.dto.UserResponse;
import com.clientos.backend.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Locked to ADMIN via SecurityConfig's /api/admin/** matcher, not annotated
// here -- see SecurityConfig for why URL-level rules were kept as the one
// authorization mechanism instead of adding @PreAuthorize alongside it.
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateAccountManagerRequest request) {
        return UserResponse.from(adminUserService.createAccountManager(request));
    }

    @GetMapping
    public List<UserResponse> list() {
        return adminUserService.listUsers().stream().map(UserResponse::from).toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        adminUserService.deleteUser(id, authentication.getName());
    }
}
