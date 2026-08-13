package com.itigotti.blog.security;

import com.itigotti.blog.domain.Role;
import org.springframework.stereotype.Service;

/**
 * 「投稿者本人 or 管理者」判定をPost/Commentで共通化するコンポーネント。
 * 判定対象はPost/Commentどちらも同じ形(リソースのauthor_idとログインユーザーを比較する)なので、
 * ドメインごとにロジックを重複させず、ここに集約している。
 */
@Service
public class AuthorizationService {

    /**
     * リソース(Post/Comment)を編集・削除できるかどうかを判定する。
     * 投稿者本人 または 管理者であればtrue。
     */
    public boolean canModify(Long resourceAuthorId, CustomUserDetails principal) {
        return principal.getRole() == Role.ADMIN || principal.getId().equals(resourceAuthorId);
    }
}