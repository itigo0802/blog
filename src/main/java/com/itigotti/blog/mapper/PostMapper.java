package com.itigotti.blog.mapper;

import com.itigotti.blog.domain.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostMapper {

    // 投稿者のusernameをJOINで含めて取得する(一覧・詳細用)。
    // keywordがnull/空文字の場合は絞り込みなしで全件返す(動的SQLの分岐はXML側)
    List<Post> findAll(@Param("keyword") String keyword);

    Post findById(Long id);

    // 単純CRUD(単一テーブルのみ)
    void insert(Post post);

    void update(Post post);

    void deleteById(Long id);
}