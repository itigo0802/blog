package com.itigotti.blog.exception;

/**
 * 管理者が自分自身をBANしようとした場合にUserService#ban()から投げられる独自例外。
 * GlobalExceptionHandlerで捕捉し、適切なHTTPステータスに変換する想定。
 */
public class SelfBanException extends RuntimeException {
    public SelfBanException(String messae) {
        super(messae);
    }
}