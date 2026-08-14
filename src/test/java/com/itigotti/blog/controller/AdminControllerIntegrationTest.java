package com.itigotti.blog.controller;

import com.itigotti.blog.domain.Role;
import com.itigotti.blog.domain.User;
import com.itigotti.blog.mapper.UserMapper;
import com.itigotti.blog.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * AdminControllerを起点に、SecurityConfigの/admin/**へのhasRole("ADMIN")制限と、
 * UserService.ban()の自己BAN防止(SelfBanException→400)をHTTPリクエスト経由で検証する結合テスト。
 * PostControllerIntegrationTestと同じ骨格(@SpringBootTest + @AutoConfigureMockMvc + @Transactional)。
 *
 * hasRole("ADMIN")はStep8実装時、ブラウザでの手動確認のみで自動テストが無かった箇所。
 * Service層のcanModify()判定と違い、SecurityConfig自体の設定をMockMvcでしか自動検証できない点が
 * PostControllerIntegrationTestとの違い。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User normalUser;
    private User admin;

    @BeforeEach
    void setUp() {
        normalUser = createUser("normal-user", Role.USER);
        admin = createUser("admin", Role.ADMIN);
    }

    @Test
    void 未ログインで管理者ページにアクセスするとログイン画面へリダイレクトされる() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void 一般ユーザーが管理者ページにアクセスすると403になる() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .with(user(principal(normalUser))))
                .andExpect(status().isForbidden());
    }

    @Test
    void 管理者は管理者ページを閲覧できる() throws Exception {
        mockMvc.perform(get("/admin/users").
                with(user(principal(admin)))).
                andExpect(status().isOk()).
                andExpect(view().name("admin/user-list"));
    }

    @Test
    void 管理者が他人をBANすると対象ユーザーはBAN状態になる() throws Exception {
        mockMvc.perform(post("/admin/users/" + normalUser.getId() + "/ban").
                with(user(principal(admin))).
                with(csrf())).
                andExpect(status().is3xxRedirection());

        assertThat(userMapper.findByUsername(normalUser.getUsername()).isEnabled()).isFalse();
    }

    @Test
    void 管理者が自分自身をBANしようとすると400になる() throws Exception {
        mockMvc.perform(post("/admin/users/" + admin.getId() + "/ban").
                with(user(principal(admin))).
                with(csrf())).
                andExpect(status().isBadRequest());
    }

    private User createUser(String username, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(role);
        user.setEnabled(true);
        userMapper.insert(user);
        return user;
    }

    private CustomUserDetails principal(User user) {
        return new CustomUserDetails(user);
    }
}