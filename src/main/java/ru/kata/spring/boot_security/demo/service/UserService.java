package ru.kata.spring.boot_security.demo.service;

import ru.kata.spring.boot_security.demo.model.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();
    User getUserById(Long id);
    User getUserByEmail(String email);
    boolean existsByEmail(String email);
    User createUser(User user, List<Long> roleIds);
    User updateUser(User updatedUser, List<Long> roleIds);
    void deleteUser(Long id);
    
}