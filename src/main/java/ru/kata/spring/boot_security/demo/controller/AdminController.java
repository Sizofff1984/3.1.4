package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public AdminController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("")
    public String showAdminPanel(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("allRoles", roleService.getAllRoles());
        model.addAttribute("user", new User());
        return "admin/admin-panel";
    }

    @PostMapping("/users/add")
    public String addUser(@ModelAttribute("user") User user,
                         @RequestParam List<Long> roleIds,
                         RedirectAttributes redirectAttributes) {
        userService.createUser(user, roleIds);
        redirectAttributes.addFlashAttribute("success", "User " + user.getEmail() + " has been added successfully!");
        return "redirect:/admin";
    }

    @PostMapping("/users/edit")
    public String editUser(@ModelAttribute("user") User user,
                           @RequestParam List<Long> roleIds,
                           RedirectAttributes redirectAttributes) {
        User updatedUser = userService.updateUser(user, roleIds);
        redirectAttributes.addFlashAttribute("success", "User " + updatedUser.getUsername() + " has been updated successfully!");
        return "redirect:/admin";
    }

    @PostMapping("/users/delete")
    public String deleteUser(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        User user = userService.getUserById(id);
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("success", "User " + user.getUsername() + " has been deleted.");
        return "redirect:/admin";
    }
}