package com.itigotti.blog.exception;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * GlobalExceptionHandlerのNoSuchElementException→404変換を、実際のHTTPリクエスト経由で検証する結合テスト。
 * PostServiceTest/CommentServiceTestのような単体テストでは「例外が投げられること」までしか確認できないが、
 * ここでは@ExceptionHandlerが実際に発火し、ステータスコードとerror/404ビューが返ることまで確認する。
 * PostController/CommentControllerではなくexceptionパッケージに置くのは、LoginIntegrationTestが
 * securityパッケージに置かれているのと同じ理由(単一コントローラーではなく横断的な仕組みの検証のため)。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GlobalExceptionHandlerIntegrationTest {

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
    private Post post;
    private Comment comment;

    @BeforeEach
    void setUp() {
        owner = createUser("owner", Role.USER);
        post = createPost("元のタイトル", "元の本文", owner.getId());
        comment = createComment("元のコメント", post.getId(), owner.getId());
    }

    @Test
    void 存在しない投稿の詳細ページは404になる() throws Exception {
        long nonExistentId = post.getId() + 1000;

        mockMvc.perform(get("/posts/" + nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));
    }

    @Test
    void 存在しない投稿の編集フォームは認証済みユーザーでも404になる() throws Exception {
        long nonExistentId = post.getId() + 1000;

        mockMvc.perform(get("/posts/" + nonExistentId + "/edit").
                with(user(principal(owner))).
                with(csrf())).
                andExpect(status().isNotFound()).
                andExpect(view().name("error/404"));
    }

    @Test
    void 存在しないコメントの削除は認証済みかつCSRFトークンありでも404になる() throws Exception {
        long nonExistentId = comment.getId() + 1000;

        mockMvc.perform(post("/posts/" + post.getId() + "/comments/" + nonExistentId + "/delete").
                with(user(principal(owner))).
                with(csrf())).
                andExpect(status().isNotFound()).
                andExpect(view().name("error/404"));
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
