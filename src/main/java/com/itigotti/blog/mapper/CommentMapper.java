package com.itigotti.blog.mapper;

import com.itigotti.blog.domain.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {

    // 投稿者のusernameをJOINで含めて取得する(投稿詳細ページのコメント一覧用)
    List<Comment> findByPostId(@Param("postId") Long postId);

    // 単純CRUD(単一テーブルのみ)
    Comment findById(Long id);

    void insert(Comment comment);

    void deleteById(Long id);
}