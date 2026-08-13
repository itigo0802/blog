package com.itigotti.blog.service;

import com.itigotti.blog.domain.Post;
import com.itigotti.blog.dto.PostForm;
import com.itigotti.blog.mapper.PostMapper;
import com.itigotti.blog.security.AuthorizationService;
import com.itigotti.blog.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final AuthorizationService authorizationService;

    public List<Post> findAll() {
        return postMapper.findAll();
    }

    public Post findById(Long id) {
        Post post = postMapper.findById(id);
        if (post == null) {
            throw new NoSuchElementException("投稿が見つかりません: id=" + id);
        }
        return post;
    }

    public void create(PostForm form, Long authorId) {
        Post post = new Post();
        post.setTitle(form.getTitle());
        post.setContent(form.getContent());
        post.setAuthorId(authorId);
        postMapper.insert(post);
    }

    public void update(Long id, PostForm form, CustomUserDetails principal) {
        Post post = findById(id);
        if (!authorizationService.canModify(post.getAuthorId(), principal)) {
            throw new AccessDeniedException("この投稿を編集する権限がありません");
        }
        post.setTitle(form.getTitle());
        post.setContent(form.getContent());
        postMapper.update(post);
    }

    public void delete(Long id, CustomUserDetails principal) {
        Post post = findById(id);
        if (!authorizationService.canModify(post.getAuthorId(), principal)) {
            throw new AccessDeniedException("この投稿を削除する権限がありません");
        }
        postMapper.deleteById(id);
    }
}