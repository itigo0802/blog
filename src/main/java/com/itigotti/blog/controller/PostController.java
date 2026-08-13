package com.itigotti.blog.controller;

import com.itigotti.blog.domain.Post;
import com.itigotti.blog.dto.CommentForm;
import com.itigotti.blog.dto.PostForm;
import com.itigotti.blog.security.CustomUserDetails;
import com.itigotti.blog.service.CommentService;
import com.itigotti.blog.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    @GetMapping("/")
    public String home() {
        return "redirect:/posts";
    }

    @GetMapping("/posts")
    public String list(Model model) {
        model.addAttribute("posts", postService.findAll());
        return "post-list";
    }

    @GetMapping("/posts/new")
    public String newForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        return "post-form";
    }

    @PostMapping("/posts")
    public String create(@Valid @ModelAttribute PostForm postForm, BindingResult bindingResult,
                          @AuthenticationPrincipal CustomUserDetails principal) {
        if (bindingResult.hasErrors()) {
            return "post-form";
        }
        postService.create(postForm, principal.getId());
        return "redirect:/posts";
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("post", postService.findById(id));
        model.addAttribute("comments", commentService.findByPostId(id));
        model.addAttribute("commentForm", new CommentForm());
        return "post-detail";
    }

    @GetMapping("/posts/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Post post = postService.findById(id);
        PostForm postForm = new PostForm();
        postForm.setTitle(post.getTitle());
        postForm.setContent(post.getContent());
        model.addAttribute("postForm", postForm);
        model.addAttribute("postId", id);
        return "post-form";
    }

    @PostMapping("/posts/{id}/edit")
    public String edit(@PathVariable Long id, @Valid @ModelAttribute PostForm postForm, BindingResult bindingResult,
                        Model model, @AuthenticationPrincipal CustomUserDetails principal) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("postId", id);
            return "post-form";
        }
        postService.update(id, postForm, principal);
        return "redirect:/posts/" + id;
    }

    @PostMapping("/posts/{id}/delete")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal) {
        postService.delete(id, principal);
        return "redirect:/posts";
    }
}