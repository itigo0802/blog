package com.itigotti.blog.service;

import com.itigotti.blog.domain.Role;
import com.itigotti.blog.domain.User;
import com.itigotti.blog.dto.RegisterForm;
import com.itigotti.blog.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public boolean isUsernameTaken(String username) {
        return userMapper.existsByUsername(username);
    }

    public boolean isEmailTaken(String email) {
        return userMapper.existsByEmail(email);
    }

    public void register(RegisterForm form) {
        User user = new User();
        user.setUsername(form.getUsername());
        user.setEmail(form.getEmail());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setRole(Role.USER);
        user.setEnabled(true);
        userMapper.insert(user);
    }
}