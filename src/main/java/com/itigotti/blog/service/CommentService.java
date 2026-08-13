package com.itigotti.blog.service;

import com.itigotti.blog.domain.Comment;
import com.itigotti.blog.dto.CommentForm;
import com.itigotti.blog.mapper.CommentMapper;
import com.itigotti.blog.security.AuthorizationService;
import com.itigotti.blog.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final AuthorizationService authorizationService;

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

    public void delete(Long id, CustomUserDetails principal) {
        Comment comment = commentMapper.findById(id);
        if (comment == null) {
            throw new NoSuchElementException("コメントが見つかりません: id=" + id);
        }
        if (!authorizationService.canModify(comment.getAuthorId(), principal)) {
            throw new AccessDeniedException("このコメントを削除する権限がありません");
        }
        commentMapper.deleteById(id);
    }
}