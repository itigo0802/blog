package com.itigotti.blog.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Comment {

    private Long id;
    private String content;
    private Long postId;
    private Long authorId;
    private LocalDateTime createdAt;

    /**
     * コメント投稿者のusername。commentsテーブル単体には存在せずusersとのJOINでのみ埋まる。
     */
    private String authorUsername;
}