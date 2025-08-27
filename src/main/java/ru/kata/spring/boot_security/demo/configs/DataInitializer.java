package ru.kata.spring.boot_security.demo.configs;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(RoleService roleService,
                                      UserService userService,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            System.out.println("=== DATABASE INITIALIZATION STARTED ===");

            // Создаем роли если их нет
            try {
                if (roleService.getAllRoles().isEmpty()) {
                    Role adminRole = new Role("ROLE_ADMIN");
                    Role userRole = new Role("ROLE_USER");
                    roleService.createRole(adminRole);
                    roleService.createRole(userRole);
                    System.out.println("✓ Roles created: ROLE_ADMIN, ROLE_USER");
                }
            } catch (Exception e) {
                System.err.println("Error creating roles: " + e.getMessage());
            }

            // Создаем тестовых пользователей если их нет
            try {
                if (userService.getAllUsers().isEmpty()) {
                    Role adminRole = roleService.getRoleByName("ROLE_ADMIN");
                    Role userRole = roleService.getRoleByName("ROLE_USER");

                    // Admin user
                    User admin = new User();
                    admin.setUsername("admin");
                    admin.setPassword(passwordEncoder.encode("admin"));
                    admin.setEmail("admin@example.com");
                    admin.setAge(30);

                    Set<Role> adminRoles = new HashSet<>();
                    adminRoles.add(adminRole);
                    adminRoles.add(userRole);
                    admin.setRoles(adminRoles);

                    userService.createUser(admin);
                    System.out.println("✓ Admin user created: admin/admin");

                    // Regular user
                    User user = new User();
                    user.setUsername("user");
                    user.setPassword(passwordEncoder.encode("user"));
                    user.setEmail("user@example.com");
                    user.setAge(25);

                    Set<Role> userRoles = new HashSet<>();
                    userRoles.add(userRole);
                    user.setRoles(userRoles);

                    userService.createUser(user);
                    System.out.println("✓ User created: user/user");
                }
            } catch (Exception e) {
                System.err.println("Error creating users: " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println("=== DATABASE INITIALIZATION COMPLETE ===");
        };
    }
}