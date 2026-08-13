package com.itigotti.blog.mapper;

import com.itigotti.blog.domain.Post;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PostMapper {

    // 投稿者のusernameをJOINで含めて取得する(一覧・詳細用)
    List<Post> findAll();

    Post findById(Long id);

    // 単純CRUD(単一テーブルのみ)
    void insert(Post post);

    void update(Post post);

    void deleteById(Long id);
}