package com.itigotti.blog.service;

import com.itigotti.blog.domain.User;
import com.itigotti.blog.mapper.UserMapper;
import com.itigotti.blog.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.findByUsername(username);
        if (user != null) {
            return new CustomUserDetails(user);
        } else {
            throw new UsernameNotFoundException(username);
        }
    }
}