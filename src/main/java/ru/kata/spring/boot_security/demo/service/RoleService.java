package ru.kata.spring.boot_security.demo.service;

import org.springframework.http.ResponseEntity;
import ru.kata.spring.boot_security.demo.model.Role;

import java.util.List;

public interface RoleService {
    List<Role> getAllRoles();
    Role getRoleById(Long id);
    Role getRoleByName(String name);
    void initRoles();
    
    // REST API method
    ResponseEntity<List<Role>> getAllRolesWithResponse();
}