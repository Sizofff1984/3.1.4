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
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(RoleService roleService,
                                      UserService userService,
                                      PasswordEncoder passwordEncoder) {
        return args -> {

            if (roleService.getAllRoles().isEmpty()) {

                Role adminRole = new Role("ROLE_ADMIN");
                Role userRole = new Role("ROLE_USER");

                roleService.createRole(adminRole);
                roleService.createRole(userRole);
                System.out.println("Roles created: ROLE_ADMIN, ROLE_USER");
            }

            if (userService.getAllUsers().isEmpty()) {

                Role adminRole = roleService.getRoleByName("ROLE_ADMIN");
                Role userRole = roleService.getRoleByName("ROLE_USER");

                Set<Role> adminRoles = new HashSet<>();
                adminRoles.add(adminRole);
                adminRoles.add(userRole);

                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin"));
                admin.setEmail("admin@example.com");
                admin.setAge(30);
                admin.setRoles(adminRoles);

                userService.createUser(admin);

                Set<Role> userRoles = new HashSet<>();
                userRoles.add(userRole);

                User user = new User();
                user.setUsername("user");
                user.setPassword(passwordEncoder.encode("user"));
                user.setEmail("user@example.com");
                user.setAge(25);
                user.setRoles(userRoles);

                userService.createUser(user);

                System.out.println("=== Test users created ===");
                System.out.println("Admin: admin / admin");
                System.out.println("User: user / user");
            } else {
                System.out.println("Users already exist in database");
            }
        };
    }
}