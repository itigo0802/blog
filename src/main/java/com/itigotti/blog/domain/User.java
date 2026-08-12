package com.itigotti.blog.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class User {

    private Long id;
    private String username;
    private String email;
    private String password;
    private Role role;
    private boolean enabled;
    private LocalDateTime createdAt;
}