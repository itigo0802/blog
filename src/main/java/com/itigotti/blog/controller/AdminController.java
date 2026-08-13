package com.itigotti.blog.controller;

import com.itigotti.blog.security.CustomUserDetails;
import com.itigotti.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理者向け機能。/admin/** はSecurityConfigでhasRole("ADMIN")に限定済み。
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    public String list(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/user-list";
    }

    @PostMapping("/users/{id}/ban")
    public String ban(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal) {
        userService.ban(id, principal);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/unban")
    public String unban(@PathVariable Long id) {
        userService.unban(id);
        return "redirect:/admin/users";
    }
}