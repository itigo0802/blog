package com.itigotti.blog.service;

import com.itigotti.blog.domain.Role;
import com.itigotti.blog.domain.User;
import com.itigotti.blog.dto.RegisterForm;
import com.itigotti.blog.exception.SelfBanException;
import com.itigotti.blog.mapper.UserMapper;
import com.itigotti.blog.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<User> findAll() {
        return userMapper.findAll();
    }

    /**
     * 管理者によるユーザーBAN(enabled=false)。
     * 呼び出し元(AdminController)は/admin/**がhasRole("ADMIN")で保護されているため、
     * principalが管理者であることはSecurityConfig側で既に保証されている。
     */
    public void ban(Long id, CustomUserDetails principal) {
        // 管理者が自分自身をBANすると誰にもアンBANしてもらえずロックアウトされるため拒否する
        if (id.equals(principal.getId())) {
            throw new SelfBanException("管理者は自分自身をBANできません: id=" + id);
        }
        userMapper.updateEnabled(id, false);
    }

    public void unban(Long id) {
        userMapper.updateEnabled(id, true);
    }
}