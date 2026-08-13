package com.itigotti.blog.service;

import com.itigotti.blog.domain.Comment;
import com.itigotti.blog.dto.CommentForm;
import com.itigotti.blog.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;

    public List<Comment> findByPostId(Long postId) {
        return commentMapper.findByPostId(postId);
    }

    public void create(Long postId, CommentForm form, Long authorId) {
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setContent(form.getContent());
        comment.setAuthorId(authorId);
        commentMapper.insert(comment);
    }

    // 投稿者本人/管理者チェックはStep6でController or ここに追加する
    public void delete(Long id) {
        commentMapper.deleteById(id);
    }
}