package ru.kata.spring.boot_security.demo.service;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repository.UserRepository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           RoleService roleService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public User createUser(User user, List<Long> roleIds) {
        if (user.getFirstName() == null || user.getLastName() == null ||
                user.getEmail() == null || user.getPassword() == null) {
            throw new RuntimeException("All required fields must be filled");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }

        if (!user.getPassword().startsWith("$2a$") &&
                !user.getPassword().startsWith("$2y$") &&
                !user.getPassword().startsWith("$10$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        setUserRoles(user, roleIds);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(User updatedUser, List<Long> roleIds) {
        User existingUser = userRepository.findById(updatedUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + updatedUser.getId()));

        if (!existingUser.getEmail().equals(updatedUser.getEmail())) {
            if (userRepository.existsByEmail(updatedUser.getEmail())) {
                throw new RuntimeException("Email already exists: " + updatedUser.getEmail());
            }
        }

        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setAge(updatedUser.getAge());
        existingUser.setEmail(updatedUser.getEmail());

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        setUserRoles(existingUser, roleIds);
        return userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }


    @Override
    @Transactional
    public ResponseEntity<Map<String, Object>> createUserWithResponse(Map<String, Object> userData) {
        try {
            User user = new User();
            user.setFirstName((String) userData.get("firstName"));
            user.setLastName((String) userData.get("lastName"));
            user.setAge((Integer) userData.get("age"));
            user.setEmail((String) userData.get("email"));
            user.setPassword((String) userData.get("password"));

            @SuppressWarnings("unchecked")
            List<Integer> roleIdsInt = (List<Integer>) userData.get("roleIds");
            List<Long> roleIds = roleIdsInt.stream()
                    .map(Integer::longValue)
                    .collect(Collectors.toList());

            User createdUser = createUser(user, roleIds);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User " + createdUser.getEmail() + " has been added successfully!");
            response.put("user", createdUser);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error creating user: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<Map<String, Object>> updateUserWithResponse(Long id, Map<String, Object> userData) {
        try {
            User existingUser = getUserById(id);
            if (existingUser == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.notFound().build();
            }

            User user = new User();
            user.setId(id);
            user.setFirstName((String) userData.get("firstName"));
            user.setLastName((String) userData.get("lastName"));
            user.setAge((Integer) userData.get("age"));
            user.setEmail((String) userData.get("email"));
            
            // Если пароль не пустой, обновляем его
            String password = (String) userData.get("password");
            if (password != null && !password.trim().isEmpty()) {
                user.setPassword(password);
            } else {
                user.setPassword(existingUser.getPassword());
            }

            @SuppressWarnings("unchecked")
            List<Integer> roleIdsInt = (List<Integer>) userData.get("roleIds");
            List<Long> roleIds = roleIdsInt.stream()
                    .map(Integer::longValue)
                    .collect(Collectors.toList());

            User updatedUser = updateUser(user, roleIds);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User " + updatedUser.getUsername() + " has been updated successfully!");
            response.put("user", updatedUser);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error updating user: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<Map<String, Object>> deleteUserWithResponse(Long id) {
        try {
            User user = getUserById(id);
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.notFound().build();
            }

            String username = user.getUsername();
            deleteUser(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User " + username + " has been deleted.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error deleting user: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private void setUserRoles(User user, List<Long> roleIds) {
        if (roleIds != null && !roleIds.isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (Long roleId : roleIds) {
                Role role = roleService.getRoleById(roleId);
                roles.add(role);
            }
            user.setRoles(roles);
        } else {
            Role userRole = roleService.getRoleByName("ROLE_USER");
            if (userRole != null) {
                user.setRoles(Set.of(userRole));
            } else {
                throw new RuntimeException("ROLE_USER not found in database");
            }
        }
    }
}