package com.itigotti.blog.security;

import com.itigotti.blog.domain.Role;
import com.itigotti.blog.domain.User;
import com.itigotti.blog.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.instanceOf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SecurityConfigのformLogin設定自体を検証する結合テスト。
 * POST /loginはAuthControllerではなくSpring Securityのフィルタ
 * (UsernamePasswordAuthenticationFilter)が処理するため、コントローラーを介さずに
 * SecurityFilterChainへ直接リクエストを投げる形になる。
 *
 * これまでのAuthControllerIntegrationTestが「登録フォームの入口」を検証していたのに対し、
 * こちらは「ログイン処理そのもの」を検証する回。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        createUser("member", "correct-password", true);
        // Step8のBAN機能で作られる、ログイン不可なユーザー
        createUser("banned-user", "correct-password", false);
    }

    // TODO(human): 以下4パターンを実装する
    // 1. 存在するユーザーが正しいユーザー名・パスワードでログインすると、/postsへリダイレクトされる。
    //    さらに .andExpect(authenticated()) を続けると、レスポンス後のSecurityContextに実際に
    //    認証情報が入っている(=ログインセッションが確立している)ことまで確認できる
    // 2. 存在するユーザーが誤ったパスワードでログインすると、/login?error へリダイレクトされる
    //    (.andExpect(unauthenticated()) でセッションが確立していないことも合わせて確認できる)
    // 3. 存在しないユーザー名でログインしようとしても、パターン2と全く同じ /login?error へ
    //    リダイレクトされる(ユーザー列挙攻撃対策として、Spring Securityが
    //    UsernameNotFoundExceptionとBadCredentialsExceptionを区別せず同じ結果にしている)
    // 4. setUp()で作成した "banned-user"(enabled=false)が正しいパスワードでログインしようとしても、
    //    DisabledExceptionとなり、結局はパターン2・3と同じ /login?error へリダイレクトされる。
    //    CLAUDE.mdに記録済みの「BANユーザーは汎用エラーしか出せない」という制限を、
    //    実際にテストコードで固定化する
    //
    // 参考: POSTのパラメータは .param("username", "...").param("password", "...") のように渡す。
    //       formLoginのデフォルトの処理URLは /login (POST) で、CSRF保護の対象なので
    //       .with(csrf()) を忘れずに。
    @Test
    void 存在するユーザーが正しいユーザー名とパスワードでログインするとpostsにリダイレクトする() throws Exception {
        mockMvc.perform(post("/login").
                param("username", "member").
                param("password", "correct-password").
                with(csrf())).
                andExpect(status().is3xxRedirection()).
                andExpect(redirectedUrl("/posts")).
                andExpect(authenticated());
    }

    @Test
    void 存在するユーザーが間違ったパスワードでログインするとエラーパラメータでloginにリダイレクトする() throws Exception {
        mockMvc.perform(post("/login").
                param("username", "member").
                param("password", "password").
                with(csrf())).
                andExpect(status().is3xxRedirection()).
                andExpect(redirectedUrl("/login?error")).
                andExpect(unauthenticated());
    }

    @Test
    void 存在しないユーザー名でログインしようとするとエラーパラメータでloginにリダイレクトする() throws Exception {
        mockMvc.perform(post("/login").
                param("username", "example").
                param("password", "correct-password").
                with(csrf())).
                andExpect(status().is3xxRedirection()).
                andExpect(redirectedUrl("/login?error")).
                andExpect(unauthenticated());
    }

    @Test
    void BANされたユーザーが正しいパスワードでログインしようとしてもDisabledExceptionになりエラーパラメータでlogin二リダイレクトする() throws Exception {
        mockMvc.perform(post("/login").
                param("username", "banned-user").
                param("password", "correct-password").
                with(csrf())).
                andExpect(request().sessionAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, instanceOf(DisabledException.class))).
                andExpect(status().is3xxRedirection()).
                andExpect(redirectedUrl("/login?error")).
                andExpect(unauthenticated());
    }

    private User createUser(String username, String rawPassword, boolean enabled) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(Role.USER);
        user.setEnabled(enabled);
        userMapper.insert(user);
        return user;
    }
}