package com.itigotti.blog.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.NoSuchElementException;

/**
 * コントローラー全体で共通の例外ハンドリング。
 * AccessDeniedExceptionはここでは扱わない(Spring Securityが自動で403に変換し、
 * templates/error/403.htmlへフォワードしてくれるため、個別に横取りする必要がない)。
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 存在しない投稿/コメントへのアクセス(NoSuchElementException)を404として返す。
     * 例外メッセージ(内部IDを含む)はログにのみ残し、画面には汎用メッセージのみ表示する。
     */
    @ExceptionHandler(NoSuchElementException.class)
    public String handleNotFound(NoSuchElementException e, HttpServletResponse response, Model model) {
        log.warn(e.getMessage(), e);
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        model.addAttribute("message", "ページが見つかりません");
        return "error/404";
    }
}