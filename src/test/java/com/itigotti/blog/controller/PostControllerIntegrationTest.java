package com.itigotti.blog.controller;

import com.itigotti.blog.domain.Post;
import com.itigotti.blog.domain.Role;
import com.itigotti.blog.domain.User;
import com.itigotti.blog.mapper.PostMapper;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * PostControllerを起点に、SecurityConfig(認証・CSRF)とService層の認可判定(投稿者本人 or 管理者)を
 * 通して検証する結合テスト。@SpringBootTestで実際のフィルタチェーン・実DB(H2)を使う点が
 * PostServiceTestのような単体テストと異なる。
 *
 * テストメソッドに@Transactionalを付けているため、各テストで書き込んだデータはテスト終了時に
 * 自動でロールバックされる(MyBatisのSqlSessionもSpring管理のトランザクションに乗るため)。
 * これにより、テストごとにDBをリセットする後始末コードが不要になる。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User owner;
    private User otherUser;
    private User admin;
    private Post post;

    @BeforeEach
    void setUp() {
        owner = createUser("owner", Role.USER);
        otherUser = createUser("other-user", Role.USER);
        admin = createUser("admin", Role.ADMIN);
        post = createPost("元のタイトル", "元の本文", owner.getId());
    }

    @Test
    void 記事一覧は未ログインでも閲覧できる() throws Exception {
        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("post-list"));
    }

    @Test
    void キーワード検索でタイトルまたは本文に一致する投稿のみ表示される() throws Exception {
        createPost("Spring入門", "Bean定義について", owner.getId());
        createPost("旅行記", "SpringでSpring Bootを学んだ話", owner.getId());
        createPost("無関係な投稿", "無関係な本文", owner.getId());

        mockMvc.perform(get("/posts").param("keyword", "Spring"))
                .andExpect(status().isOk())
                .andExpect(view().name("post-list"))
                .andExpect(model().attribute("posts", hasSize(2)));
    }

    @Test
    void 一致しないキーワードでは0件になる() throws Exception {
        mockMvc.perform(get("/posts").param("keyword", "存在しないキーワード"))
                .andExpect(status().isOk())
                .andExpect(view().name("post-list"))
                .andExpect(model().attribute("posts", hasSize(0)));
    }

    @Test
    void 未ログインで記事投稿しようとするとログイン画面へリダイレクトされる() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("title", "タイトル")
                        .param("content", "本文")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void 投稿者本人による編集は成功する() throws Exception {
        CustomUserDetails principal = principal(owner);

        mockMvc.perform(post("/posts/" + post.getId() + "/edit").
                param("title", "編集されたタイトル").
                param("content", "編集されたコンテンツ").
                with(csrf()).
                with(user(principal))).
                andExpect(status().is3xxRedirection()).
                andExpect(redirectedUrl("/posts/" + post.getId()));
    }

    @Test
    void 投稿者本人でも管理者でもないユーザーの編集は403になる() throws Exception {
        mockMvc.perform(post("/posts/" + post.getId() + "/edit").
                param("title", "編集されたタイトル").
                param("content", "編集されたコンテンツ").
                with(csrf()).
                with(user(principal(otherUser)))).
                andExpect(status().isForbidden());
    }

    @Test
    void 管理者は他人の投稿でも削除できる() throws Exception {
        mockMvc.perform(post("/posts/" + post.getId() + "/delete").
                with(user(principal(admin))).
                with(csrf())).
                andExpect(status().is3xxRedirection()).
                andExpect(redirectedUrl("/posts"));
    }

    @Test
    void CSRFトークンなしはたとえ投稿者本人でも403になる() throws Exception {
        mockMvc.perform(post("/posts/" + post.getId() + "/edit").
                param("title", "編集されたタイトル").
                param("content", "編集されたコンテンツ").
                with(user(principal(owner)))).
                andExpect(status().isForbidden());
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

    private Post createPost(String title, String content, Long authorId) {
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setAuthorId(authorId);
        postMapper.insert(post);
        return post;
    }

    private CustomUserDetails principal(User user) {
        return new CustomUserDetails(user);
    }
}