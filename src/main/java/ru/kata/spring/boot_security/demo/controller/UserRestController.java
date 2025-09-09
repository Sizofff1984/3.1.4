package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;

@Controller
@RequestMapping("/api/user")
public class UserRestController {

    private final UserService userService;

    @Autowired
    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfile(Authentication authentication) {
        try {
            User user = userService.getCurrentUserWithResponse(authentication);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Web endpoint for HTML page
    @GetMapping
    public String showUserProfile(Authentication authentication, Model model) {
        try {
            User user = userService.getCurrentUserWithResponse(authentication);
            model.addAttribute("user", user);
            return "user/profile";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }
}


