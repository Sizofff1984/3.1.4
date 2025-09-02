package ru.kata.spring.boot_security.demo.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repository.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        System.out.println("=== НАЧАЛО updateUser. HASH: " + this.hashCode() + " ===");
        System.out.println("Текущая транзакция: " + TransactionSynchronizationManager.getCurrentTransactionName());
        System.out.println("Внутри транзакции: " + TransactionSynchronizationManager.isActualTransactionActive());
        System.out.println("ID полученного пользователя: " + (updatedUser != null ? updatedUser.getId() : "null"));

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

        Set<Role> roles = new HashSet<>();
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                Role role = roleService.getRoleById(roleId);
                roles.add(role);
            }
        } else {
            roles.add(roleService.getRoleByName("ROLE_USER"));
        }
        existingUser.setRoles(roles);

        User savedUser = userRepository.save(existingUser);
        System.out.println("=== ПОСЛЕ save(). ID в БД: " + savedUser.getId());
        System.out.println("Пароль сохранен: " + (savedUser.getPassword() != null));
        return savedUser;
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void initAdminUser() {
        if (userRepository.findByEmail("admin@mail.ru").isEmpty()) {
            User admin = new User();
            admin.setEmail("admin@mail.ru");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setAge(30);
            admin.setFirstName("Admin");
            admin.setLastName("User");

            Set<Role> roles = new HashSet<>();
            Role adminRole = roleService.getRoleByName("ROLE_ADMIN");
            if (adminRole == null) {
                throw new RuntimeException("ROLE_ADMIN not found in database");
            }
            roles.add(adminRole);
            admin.setRoles(roles);

            userRepository.save(admin);
        }
    }

    @Override
    @Transactional
    public void initUser() {
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