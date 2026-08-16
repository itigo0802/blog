package com.itigotti.blog.service;

import com.itigotti.blog.domain.Post;
import com.itigotti.blog.domain.Role;
import com.itigotti.blog.domain.User;
import com.itigotti.blog.dto.PostForm;
import com.itigotti.blog.mapper.PostMapper;
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
 * PostServiceはPostMapper(DB)に依存するため、UserServiceTestと同様にMockitoでモック化してテストする。
 * AuthorizationServiceはAuthorizationServiceTestで確認した通り依存を持たない純粋ロジックなので、
 * ここではモックにせずnew AuthorizationService()の実インスタンスを組み合わせる
 * (「投稿者本人 or 管理者」判定そのものの正しさは既にAuthorizationServiceTestが担保しているため、
 * ここではPostServiceがその判定結果を受けて正しくMapperを呼ぶ/呼ばないかに集中する)。
 */
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostMapper postMapper;

    private PostService postService;

    @BeforeEach
    void setUp() {
        postService = new PostService(postMapper, new AuthorizationService());
    }

    @Test
    void 存在しないidのfindByIdはNoSuchElementException() {
        when(postMapper.findById(999L)).thenReturn(null);

        assertThatThrownBy(() -> postService.findById(999L)).isInstanceOf(NoSuchElementException.class);
    }

    // TODO(human)
    // 以下2パターンを実装する(update()を対象、delete()もほぼ同じ形になる):
    //
    // 1. 投稿者本人による更新は成功し、postMapper.update(...)が呼ばれる
    //    → when(postMapper.findById(id)).thenReturn(post(id, 本人のid)) でモックを準備してから
    //      postService.update(id, form(...), principal(本人のid, Role.USER)) を呼び、
    //      verify(postMapper).update(...) で呼ばれたことを確認する
    //
    // 2. 投稿者本人でも管理者でもないユーザーによる更新はAccessDeniedExceptionをthrowし、
    //    postMapper.update(...)は呼ばれない
    //    → UserServiceTestの「自分自身をBANしようとするとSelfBanException」と同じ形
    //      (assertThatThrownBy + verify(postMapper, never()).update(any()))
    @Test
    void 投稿者本人による更新は成功してpostMapperが呼ばれる() {
        Post post = post(1L, 1L);
        when(postMapper.findById(1L)).thenReturn(post);

        postService.update(1L, form("更新されたタイトル", "更新されたコンテンツ"), principal(1L, Role.USER));
        verify(postMapper).update(post);
    }

    @Test
    void 投稿者でも管理者でもないユーザーによる更新はAccessDeniedExceptionをthrowする() {
        when(postMapper.findById(1L)).thenReturn(post(1L, 1L));

        assertThatThrownBy(() -> postService.update(1L, form("更新されたタイトル", "更新されたコンテンツ"), principal(2L, Role.USER))).isInstanceOf(AccessDeniedException.class);
        verify(postMapper, never()).update(any());
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

    private Post post(Long id, Long authorId) {
        Post post = new Post();
        post.setId(id);
        post.setTitle("元のタイトル");
        post.setContent("元の本文");
        post.setAuthorId(authorId);
        return post;
    }

    private PostForm form(String title, String content) {
        PostForm form = new PostForm();
        form.setTitle(title);
        form.setContent(content);
        return form;
    }
}
