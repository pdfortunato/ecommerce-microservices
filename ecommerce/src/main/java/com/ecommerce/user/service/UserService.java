package com.ecommerce.user.service;

import com.ecommerce.user.model.User;

import java.util.Optional;

public interface UserService {

    User registerUser(String username, String email, String password, String role);
    String authenticateUser(String username, String password);
    Optional<User> findByEmail(String email);
}
