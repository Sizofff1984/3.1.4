package ru.kata.spring.boot_security.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserService userService;
    private final RoleService roleService;

    public AdminController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/users")
    public String showAllUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    @GetMapping("/users/add")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("allRoles", roleService.getAllRoles());
        return "admin/add-user";
    }

    @PostMapping("/users/add")
    public String addUser(@Valid @ModelAttribute("user") User user,
                          @RequestParam(value = "roles", required = false) List<Long> roleIds,
                          BindingResult result,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/add-user";
        }

        if (roleIds != null && !roleIds.isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (Long roleId : roleIds) {
                Role role = roleService.getAllRoles().stream()
                        .filter(r -> r.getId().equals(roleId))
                        .findFirst()
                        .orElse(null);
                if (role != null) {
                    roles.add(role);
                }
            }
            user.setRoles(roles);
        }

        userService.createUser(user);
        redirectAttributes.addFlashAttribute("success", "User added successfully!");
        return "redirect:/admin/users";
    }

    @GetMapping("/users/edit")
    public String showEditForm(@RequestParam Long id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        model.addAttribute("allRoles", roleService.getAllRoles());
        return "admin/edit-user";
    }

    @PostMapping("/users/edit")
    public String updateUser(@Valid @ModelAttribute("user") User user,
                             @RequestParam(value = "roles", required = false) List<Long> roleIds,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/edit-user";
        }

        if (roleIds != null && !roleIds.isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (Long roleId : roleIds) {
                Role role = roleService.getAllRoles().stream()
                        .filter(r -> r.getId().equals(roleId))
                        .findFirst()
                        .orElse(null);
                if (role != null) {
                    roles.add(role);
                }
            }
            user.setRoles(roles);
        }

        userService.updateUser(user);
        redirectAttributes.addFlashAttribute("success", "User updated successfully!");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete")
    public String deleteUser(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
        return "redirect:/admin/users";
    }
    @GetMapping("/db-info")
    @ResponseBody
    public String showDbInfo() {
        List<User> users = userService.getAllUsers();
        StringBuilder info = new StringBuilder();

        info.append("<h2>Users in database:</h2>");
        for (User user : users) {
            info.append("ID: ").append(user.getId())
                    .append(", Username: ").append(user.getUsername())
                    .append(", Password: ").append(user.getPassword())
                    .append(", Email: ").append(user.getEmail())
                    .append(", Roles: ").append(user.getRoles())
                    .append("<br>");
        }

        return info.toString();
    }
}