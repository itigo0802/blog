package com.itigotti.blog.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Post {

    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 投稿者のusername。posts単体には存在せずusersとのJOINでのみ埋まる。
     * 単純CRUD(insert/update/delete)では使用しない。
     */
    private String authorUsername;
}