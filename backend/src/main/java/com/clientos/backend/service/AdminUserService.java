package com.clientos.backend.service;

import com.clientos.backend.dto.CreateAccountManagerRequest;
import com.clientos.backend.entity.Role;
import com.clientos.backend.entity.User;
import com.clientos.backend.exception.EmailAlreadyExistsException;
import com.clientos.backend.exception.SelfDeletionNotAllowedException;
import com.clientos.backend.exception.UserNotFoundException;
import com.clientos.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Always ACCOUNT_MANAGER -- this endpoint's only purpose is letting an
    // Admin onboard Account Managers, never another Admin.
    @Transactional
    public User createAccountManager(CreateAccountManagerRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = new User(
                request.name(),
                request.email(),
                passwordEncoder.encode(request.password()),
                Role.ACCOUNT_MANAGER
        );
        return userRepository.save(user);
    }

    public List<User> listUsers() {
        return userRepository.findAll();
    }

    // Deleting a user who still owns clients, uploaded documents, or has
    // client-request rows (requested_by/decided_by) fails with a foreign
    // key violation -- there's no cascade or reassignment here on purpose,
    // since silently orphaning or reassigning someone's clients is a much
    // bigger decision than this button should make on its own. That
    // failure surfaces as a normal 400 through GlobalExceptionHandler's
    // existing DataIntegrityViolationException handler.
    @Transactional
    public void deleteUser(Long userId, String actingAdminEmail) {
        User actingAdmin = userRepository.findByEmail(actingAdminEmail)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + actingAdminEmail));
        if (actingAdmin.getId().equals(userId)) {
            throw new SelfDeletionNotAllowedException();
        }

        User target = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        userRepository.delete(target);
    }
}
