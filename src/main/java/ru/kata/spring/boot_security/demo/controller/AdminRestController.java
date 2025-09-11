package ru.kata.spring.boot_security.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/admin")
public class AdminRestController {

    private final UserService userService;
    private final RoleService roleService;

    public AdminRestController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping(value = "/users", produces = "application/json")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, Object> userData) {
        User user = createUserFromData(userData);
        List<Long> roleIds = extractRoleIds(userData);
        User createdUser = userService.createUser(user, roleIds);
        return ResponseEntity.ok(createSuccessResponse("User " + createdUser.getEmail() + " has been added successfully!", createdUser));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> userData) {
        User existingUser = userService.getUserById(id);
        User user = createUserFromData(userData);
        user.setId(id);
        
        String password = (String) userData.get("password");
        if (password == null || password.trim().isEmpty()) {
            user.setPassword(existingUser.getPassword());
        }
        
        List<Long> roleIds = extractRoleIds(userData);
        User updatedUser = userService.updateUser(user, roleIds);
        return ResponseEntity.ok(createSuccessResponse("User " + updatedUser.getUsername() + " has been updated successfully!", updatedUser));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        String username = user.getUsername();
        userService.deleteUser(id);
        return ResponseEntity.ok(createSuccessResponse("User " + username + " has been deleted.", null));
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @GetMapping("/current-user")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            User user = userService.getUserByEmail(email);
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping(value = "/panel", produces = "text/html")
    public String showAdminPanel(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/admin-panel";
    }

    @GetMapping(value = "/add-user", produces = "text/html")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", roleService.getAllRoles());
        return "admin/add-user";
    }

    @GetMapping(value = "/edit-user/{id}", produces = "text/html")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        model.addAttribute("roles", roleService.getAllRoles());
        return "admin/edit-user";
    }

    private User createUserFromData(Map<String, Object> userData) {
        User user = new User();
        user.setFirstName((String) userData.get("firstName"));
        user.setLastName((String) userData.get("lastName"));
        user.setAge((Integer) userData.get("age"));
        user.setEmail((String) userData.get("email"));
        user.setPassword((String) userData.get("password"));
        return user;
    }

    @SuppressWarnings("unchecked")
    private List<Long> extractRoleIds(Map<String, Object> userData) {
        List<Integer> roleIdsInt = (List<Integer>) userData.get("roleIds");
        return roleIdsInt.stream()
                .map(Integer::longValue)
                .collect(Collectors.toList());
    }

    private Map<String, Object> createSuccessResponse(String message, User user) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        if (user != null) {
            response.put("user", user);
        }
        return response;
    }
}