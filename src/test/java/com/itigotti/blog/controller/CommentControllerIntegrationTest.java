package com.itigotti.blog.controller;

import com.itigotti.blog.domain.Comment;
import com.itigotti.blog.domain.Post;
import com.itigotti.blog.domain.Role;
import com.itigotti.blog.domain.User;
import com.itigotti.blog.mapper.CommentMapper;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * CommentControllerを起点に、コメント削除の認可判定(投稿者本人 or 管理者、AuthorizationService経由)を
 * PostControllerIntegrationTestと同じ骨格で検証する結合テスト。
 * コメントの「投稿者」はコメント自体のauthor_idであり、コメントがぶら下がる記事の投稿者とは無関係な点に注意。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User owner;
    private User otherUser;
    private User admin;
    private Post post;
    private Comment comment;

    @BeforeEach
    void setUp() {
        owner = createUser("owner", Role.USER);
        otherUser = createUser("other-user", Role.USER);
        admin = createUser("admin", Role.ADMIN);
        // コメントがぶら下がる記事自体の投稿者は誰でもよいので、ここではadminにしておく
        post = createPost("元のタイトル", "元の本文", admin.getId());
        comment = createComment("元のコメント", post.getId(), owner.getId());
    }

    @Test
    void 記事詳細ページは未ログインでも閲覧できる() throws Exception {
        mockMvc.perform(get("/posts/" + post.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("post-detail"));
    }

    @Test
    void 未ログインでコメント投稿しようとするとログイン画面へリダイレクトされる() throws Exception {
        mockMvc.perform(post("/posts/" + post.getId() + "/comments")
                        .param("content", "コメント本文")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void コメント投稿者による削除は成功する() throws Exception {
        mockMvc.perform(post("/posts/" + post.getId() + "/comments/" + comment.getId() + "/delete").
                with(user(principal(owner))).
                with(csrf())).
                andExpect(status().is3xxRedirection()).
                andExpect(redirectedUrl("/posts/" + post.getId()));
    }

    @Test
    void コメント投稿者本人でも管理者でもないユーザーによる削除は403になる() throws Exception {
        mockMvc.perform(post("/posts/" + post.getId() + "/comments/" + comment.getId() + "/delete").
                with(user(principal(otherUser))).
                with(csrf())).
                andExpect(status().isForbidden());
    }

    @Test
    void 管理者は他人のコメントでも削除できる() throws Exception {
        mockMvc.perform(post("/posts/" + post.getId() + "/comments/" + comment.getId() + "/delete").
                with(user(principal(admin))).
                with(csrf())).
                andExpect(status().is3xxRedirection()).
                andExpect(redirectedUrl("/posts/" + post.getId()));
    }

    @Test
    void CSRFトークンなしのポストはたとえ投稿者本人でも403になる() throws Exception {
        mockMvc.perform(post("/posts/" + post.getId() + "/comments/" + comment.getId() + "/delete").
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

    private Comment createComment(String content, Long postId, Long authorId) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setPostId(postId);
        comment.setAuthorId(authorId);
        commentMapper.insert(comment);
        return comment;
    }

    private CustomUserDetails principal(User user) {
        return new CustomUserDetails(user);
    }
}