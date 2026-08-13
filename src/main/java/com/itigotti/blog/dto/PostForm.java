package com.itigotti.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostForm {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    private String content;
}