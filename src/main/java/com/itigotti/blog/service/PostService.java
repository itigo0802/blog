package com.itigotti.blog.service;

import com.itigotti.blog.domain.Post;
import com.itigotti.blog.dto.PostForm;
import com.itigotti.blog.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;

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

    // 投稿者本人/管理者チェックはStep6でController or ここに追加する
    public void update(Long id, PostForm form) {
        Post post = findById(id);
        post.setTitle(form.getTitle());
        post.setContent(form.getContent());
        postMapper.update(post);
    }

    public void delete(Long id) {
        postMapper.deleteById(id);
    }
}