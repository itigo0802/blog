package com.itigotti.blog.security;

import com.itigotti.blog.domain.Role;
import com.itigotti.blog.domain.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AuthorizationServiceはSpring管理下のBeanだが、フィールドを持たない純粋なロジックなので
 * @SpringBootTestでコンテナを起動せず、直接インスタンス化してテストする。
 */
class AuthorizationServiceTest {

    private final AuthorizationService authorizationService = new AuthorizationService();

    @Test
    void 投稿者本人ならtrue() {
        CustomUserDetails principal = principal(1L, Role.USER);

        assertThat(authorizationService.canModify(1L, principal)).isTrue();
    }

    @Test
    void 管理者は他人のリソースでもtrue() {
        CustomUserDetails principal = principal(1L, Role.ADMIN);

        assertThat(authorizationService.canModify(2L, principal)).isTrue();
    }

    @Test
    void 管理者でも本人でもない場合はfalse() {
        CustomUserDetails principal = principal(1L, Role.USER);

        assertThat(authorizationService.canModify(2L, principal)).isFalse();
    }

    @Test
    void idが128以上でも本人チェックが正しく動く() {
        CustomUserDetails principal = principal(200L, Role.USER);

        assertThat(authorizationService.canModify(200L, principal)).isTrue();
    }

    private CustomUserDetails principal(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setUsername("test-user");
        user.setPassword("password");
        user.setRole(role);
        user.setEnabled(true);
        return new CustomUserDetails(user);
    }
}