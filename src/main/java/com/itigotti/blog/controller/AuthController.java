package com.itigotti.blog.controller;

import com.itigotti.blog.dto.RegisterForm;
import com.itigotti.blog.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterForm registerForm, BindingResult bindingResult) {
        if (userService.isUsernameTaken(registerForm.getUsername())) {
            bindingResult.rejectValue("username", "duplicate", "このユーザー名は既に使われています");
        }
        if (userService.isEmailTaken(registerForm.getEmail())) {
            bindingResult.rejectValue("email", "duplicate", "このメールアドレスは既に使われています");
        }
        if (bindingResult.hasErrors()) {
            return "register";
        }

        userService.register(registerForm);
        return "redirect:/login?registered";
    }
}