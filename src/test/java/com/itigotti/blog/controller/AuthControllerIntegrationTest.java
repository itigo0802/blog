package com.itigotti.blog.controller;

import com.itigotti.blog.domain.Role;
import com.itigotti.blog.domain.User;
import com.itigotti.blog.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * AuthControllerの結合テスト。これまでのPost/Comment/AdminControllerIntegrationTestと違い、
 * 「投稿者本人 or 管理者」のような認可判定は登場しない。代わりに会員登録フォームの
 * バリデーション(Bean Validation + ユーザー名/メール重複チェック)とCSRF保護が検証対象になる。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // 重複チェック系のテストで使う、既存の登録済みユーザー
        createUser("existing-user", "existing@example.com");
    }

    @Test
    void ログインページは誰でも閲覧できる() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void 登録ページは誰でも閲覧できる() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerForm"));
    }

    @Test
    void 有効な入力で登録処理を正常に行いかつDBに保存されたユーザーのパスワードが平文ではなくハッシュ化されている() throws Exception {
        mockMvc.perform(post("/register").
                param("username", "example").
                param("email", "register@example.com").
                param("password", "Hoge1234").
                with(csrf())).
                andExpect(status().is3xxRedirection()).
                andExpect(redirectedUrl("/login?registered"));

        String dbRegisteredPassword = userMapper.findByUsername("example").getPassword();
        assertThat(passwordEncoder.matches("Hoge1234", dbRegisteredPassword)).isTrue();
    }

    @Test
    void 作成済みのユーザー名と同じユーザー名を登録しようとするとリダイレクトせずエラーが出る() throws Exception {
        mockMvc.perform(post("/register").
                param("username", "existing-user").
                param("email", "existing@example.com").
                param("password", "Hoge1234").
                with(csrf())).
                andExpect(view().name("register")).
                andExpect(status().is2xxSuccessful()).
                andExpect(model().attributeHasFieldErrors("registerForm", "username"));
    }

    @Test
    void RegisterFormのValidationに違反する入力で登録しようとするとリダイレクトせずエラーが出る() throws Exception {
        mockMvc.perform(post("/register").
                param("username", "ab").
                param("email", "abcd").
                param("password", "abcd").
                with(csrf())).
                andExpect(status().is2xxSuccessful()).
                andExpect(view().name("register")).
                andExpect(model().attributeHasErrors("registerForm"));
    }

    @Test
    void 有効な入力でもCSRFトークンなしなら403になる() throws Exception {
        mockMvc.perform(post("/register").
                param("username", "example").
                param("email", "register@example.com").
                param("password", "Hoge1234")).
                andExpect(status().isForbidden());
    }

    private User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(Role.USER);
        user.setEnabled(true);
        userMapper.insert(user);
        return user;
    }
}