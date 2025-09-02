package ru.kata.spring.boot_security.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String showUserProfile(Authentication authentication, Model model) {
        String email = authentication.getName();
        System.out.println("Looking for user with email: " + email);
        
        try {
            User user = userService.getUserByEmail(email);
            System.out.println("Found user: " + user.getEmail() + " with ID: " + user.getId());
            model.addAttribute("user", user);
        } catch (RuntimeException e) {
            System.out.println("Error finding user: " + e.getMessage());
            model.addAttribute("user", null);
        }
        
        return "user/profile";
    }
}