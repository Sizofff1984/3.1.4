package ru.kata.spring.boot_security.demo.configs;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repository.UserRepository;
import ru.kata.spring.boot_security.demo.service.RoleService;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {
    private final RoleService roleService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleService roleService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleService = roleService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        roleService.initRoles();
        initAdminUser();
        initUser();
    }

    private void initAdminUser() {
        if (userRepository.findByEmail("admin@mail.ru").isEmpty()) {
            User admin = new User();
            admin.setEmail("admin@mail.ru");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setAge(30);
            admin.setFirstName("Admin");
            admin.setLastName("Admin");

            Set<Role> roles = new HashSet<>();
            Role adminRole = roleService.getRoleByName("ROLE_ADMIN");
            Role userRole = roleService.getRoleByName("ROLE_USER");
            
            if (adminRole == null) {
                throw new RuntimeException("ROLE_ADMIN not found in database");
            }
            if (userRole == null) {
                throw new RuntimeException("ROLE_USER not found in database");
            }
            
            roles.add(adminRole);
            roles.add(userRole);
            admin.setRoles(roles);

            userRepository.save(admin);
        }
    }

    private void initUser() {
        if (userRepository.findByEmail("user@mail.ru").isEmpty()) {
            User user = new User();
            user.setEmail("user@mail.ru");
            user.setPassword(passwordEncoder.encode("user"));
            user.setFirstName("User");
            user.setLastName("User");
            user.setAge(30);

            Set<Role> roles = new HashSet<>();
            Role userRole = roleService.getRoleByName("ROLE_USER");
            if (userRole != null) {
                roles.add(userRole);
            } else {
                throw new RuntimeException("ROLE_USER not found in database");
            }
            user.setRoles(roles);

            userRepository.save(user);
        }
    }
}