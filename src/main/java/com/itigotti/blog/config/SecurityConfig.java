package com.itigotti.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // TODO(human)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.authorizeHttpRequests(auth ->
                auth.requestMatchers("/admin/**").hasRole("ADMIN").
                requestMatchers("/posts/{id}/edit", "/posts/{id}/delete").authenticated().
                requestMatchers(HttpMethod.POST, "/posts", "/posts/{id}/comments").authenticated().
                requestMatchers(HttpMethod.GET, "/login").permitAll().
                requestMatchers(HttpMethod.GET, "/register").permitAll().
                requestMatchers(HttpMethod.POST, "/register").permitAll().
                requestMatchers(HttpMethod.GET, "/", "/posts", "/posts/{id}").permitAll().
                anyRequest().authenticated()).
        formLogin(form ->
                form.loginPage("/login").
                permitAll().
                defaultSuccessUrl("/posts"));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}