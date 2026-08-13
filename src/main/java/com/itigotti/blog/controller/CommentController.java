package com.itigotti.blog.controller;

import com.itigotti.blog.dto.CommentForm;
import com.itigotti.blog.security.CustomUserDetails;
import com.itigotti.blog.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    public String create(@PathVariable Long postId, @Valid @ModelAttribute CommentForm commentForm,
                          BindingResult bindingResult, @AuthenticationPrincipal CustomUserDetails principal) {
        // バリデーションエラー時は投稿詳細ページの再描画が必要だが、
        // コメント一覧の再取得が必要になるためStep6以降でPostControllerと合わせて整理する。
        // 今はシンプルにコメント欄を空のままリダイレクトする。
        if (!bindingResult.hasErrors()) {
            commentService.create(postId, commentForm, principal.getId());
        }
        return "redirect:/posts/" + postId;
    }

    @PostMapping("/posts/{postId}/comments/{commentId}/delete")
    public String delete(@PathVariable Long postId, @PathVariable Long commentId) {
        commentService.delete(commentId);
        return "redirect:/posts/" + postId;
    }
}