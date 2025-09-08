package ru.kata.spring.boot_security.demo.service;

import org.springframework.http.ResponseEntity;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;

import java.util.List;
import java.util.Map;

public interface UserService {
    List<User> getAllUsers();
    User getUserById(Long id);
    User getUserByEmail(String email);
    boolean existsByEmail(String email);
    User createUser(User user, List<Long> roleIds);
    User updateUser(User updatedUser, List<Long> roleIds);
    void deleteUser(Long id);
    
    // REST API methods
    ResponseEntity<Map<String, Object>> createUserWithResponse(Map<String, Object> userData);
    ResponseEntity<Map<String, Object>> updateUserWithResponse(Long id, Map<String, Object> userData);
    ResponseEntity<Map<String, Object>> deleteUserWithResponse(Long id);
}