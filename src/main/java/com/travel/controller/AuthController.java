package com.travel.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.travel.entity.User;
import com.travel.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    // ================= SERVICE =================

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // ================= INDEX =================

    @GetMapping("/")
    public String index() {
        return "index";
    }

    // ================= REGISTER PAGE =================

    @GetMapping("/register")
    public String registerPage(Model model) {

        model.addAttribute("user", new User());

        return "register";
    }

    // ================= REGISTER =================

    @PostMapping("/register")
    public String register(
            @ModelAttribute User user,
            @RequestParam("photoFile") MultipartFile photoFile,
            Model model) throws IOException {

        // SAVE IMAGE

        if (!photoFile.isEmpty()) {

            user.setImageName(
                    photoFile.getOriginalFilename());

            user.setImageData(
                    photoFile.getBytes());
        }

        // REGISTER USER

        String result =
                userService.register(user);

        // ERROR

        if (!result.equals("success")) {

            model.addAttribute(
                    "error",
                    result);

            return "register";
        }

        // SUCCESS

        model.addAttribute(
                "success",
                "Registration Successful");

        return "login";
    }

    // ================= LOGIN PAGE =================

    @GetMapping("/login")
    public String loginPage(HttpSession session) {

        if (session.getAttribute("username") != null) {

            String role =
                    (String) session.getAttribute("role");

            if ("ADMIN".equalsIgnoreCase(role)) {

                return "redirect:/admin";
            }

            return "redirect:/dashboard";
        }

        return "login";
    }

    // ================= LOGIN =================

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        boolean isValid =
                userService.login(username, password);

        if (isValid) {

            User user =
                    userService.getUserByUsername(username);

            // STORE SESSION DATA

            session.setAttribute(
                    "username",
                    user.getUsername());

            session.setAttribute(
                    "email",
                    user.getEmail());

            session.setAttribute(
                    "role",
                    user.getRole());

            // ADMIN REDIRECT

            if ("ADMIN".equalsIgnoreCase(user.getRole())) {

                return "redirect:/admin";
            }

            // USER REDIRECT

            return "redirect:/dashboard";
        }

        // INVALID LOGIN

        model.addAttribute(
                "error",
                "Invalid Username or Password");

        return "login";
    }

    // ================= DASHBOARD =================

    @GetMapping("/dashboard")
    public String dashboard(
            HttpSession session,
            Model model) {

        String username =
                (String) session.getAttribute("username");

        String email =
                (String) session.getAttribute("email");

        model.addAttribute(
                "username",
                username);

        model.addAttribute(
                "email",
                email);

        return "dashboard";
    }

    // ================= USER IMAGE =================

    @GetMapping("/user/image/{username}")
    public ResponseEntity<byte[]> getUserImage(
            @PathVariable String username) {

        User user =
                userService.getUserByUsername(username);

        if (user == null || user.getImageData() == null) {

            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()

                .contentType(MediaType.IMAGE_JPEG)

                .body(user.getImageData());
    }

    // ================= LOGOUT =================

    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/login?logout";
    }
}