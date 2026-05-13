package com.travel.service;

import com.travel.entity.User;
import com.travel.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    // REGISTER
    public String register(User user) {

        if (repo.findByUsername(user.getUsername()) != null) {
            return "Username already exists";
        }

        // 🔐 Encrypt password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // ✅ SET DEFAULT ROLE
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }

        repo.save(user);
        return "success";
    }

    // LOGIN
    public boolean login(String username, String password) {

        User user = repo.findByUsername(username);

        if (user == null) return false;

        return passwordEncoder.matches(password, user.getPassword());
    }

    // ✅ GET USER (for role)
    public User getUserByUsername(String username) {
        return repo.findByUsername(username);
    }
}