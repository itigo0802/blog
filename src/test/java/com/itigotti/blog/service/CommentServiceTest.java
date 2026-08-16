package com.itigotti.blog.service;

import com.itigotti.blog.domain.Comment;
import com.itigotti.blog.domain.Role;
import com.itigotti.blog.domain.User;
import com.itigotti.blog.mapper.CommentMapper;
import com.itigotti.blog.security.AuthorizationService;
import com.itigotti.blog.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * CommentServiceはCommentMapper(DB)に依存するため、PostServiceTestと同様にMockitoでモック化する。
 * AuthorizationServiceはPostServiceTestと同じ理由(依存を持たない純粋ロジック)でモックにせず実インスタンスを使う。
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentMapper commentMapper;

    private CommentService commentService;

    @BeforeEach
    void setUp() {
        commentService = new CommentService(commentMapper, new AuthorizationService());
    }

    @Test
    void 存在しないidのdeleteはNoSuchElementException() {
        when(commentMapper.findById(999L)).thenReturn(null);

        assertThatThrownBy(() -> commentService.delete(999L, principal(1L, Role.USER)))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void コメント投稿者による削除は成功しcommentMapperが呼ばれる() {
        when(commentMapper.findById(1L)).thenReturn(comment(1L, 1L));

        commentService.delete(1L, principal(1L, Role.USER));
        verify(commentMapper).deleteById(1L);
    }

    @Test
    void コメント投稿者でも管理者でもないユーザーによる削除はAccessDeniedExeptionがthrowされる() {
        when(commentMapper.findById(1L)).thenReturn(comment(1L, 1L));

        assertThatThrownBy(() -> commentService.delete(1L, principal(2L, Role.USER))).isInstanceOf(AccessDeniedException.class);
        verify(commentMapper, never()).deleteById(any());
    }

    private CustomUserDetails principal(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setUsername("test-user");
        user.setPassword("password");
        user.setRole(role);
        user.setEnabled(true);
        return new CustomUserDetails(user);
    }

    private Comment comment(Long id, Long authorId) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setContent("元のコメント");
        comment.setAuthorId(authorId);
        return comment;
    }
}
