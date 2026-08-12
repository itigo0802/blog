package com.itigotti.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

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
                // Spring Bootが404などを内部フォワードする先。許可しないと未ログイン時に意図せずログイン画面へ飛ばされる
                requestMatchers("/error").permitAll().
                // 開発用DBコンソール(H2)。本番運用するアプリでは絶対に開放しないこと
                requestMatchers("/h2-console/**").permitAll().
                anyRequest().authenticated()).
        formLogin(form ->
                form.loginPage("/login").
                permitAll().
                defaultSuccessUrl("/posts")).
        // H2コンソールはiframeで自身のページを埋め込むため、デフォルトのX-Frame-Options: DENYだと表示できない
        headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin)).
        // H2コンソールは自前のフォーム送信にCSRFトークンを付与しないため、このパスのみ除外する
        csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}