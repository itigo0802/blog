package com.itigotti.blog.service;

import com.itigotti.blog.domain.Role;
import com.itigotti.blog.domain.User;
import com.itigotti.blog.exception.SelfBanException;
import com.itigotti.blog.mapper.UserMapper;
import com.itigotti.blog.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * UserServiceはUserMapper(DB)に依存するため、AuthorizationServiceTestと違い
 * Mockitoでモック化してテストする。@SpringBootTestでコンテナを起動する必要はない。
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userMapper, passwordEncoder);
    }

    @Test
    void 他人はBANできる() {
        CustomUserDetails principal = principal(1L);

        userService.ban(2L, principal);

        verify(userMapper).updateEnabled(2L, false);
    }

    // TODO(human): 自分自身をBANしようとした場合のテストを書く
    // - SelfBanExceptionがthrowされること
    //   (AssertJのassertThatThrownBy(() -> userService.ban(1L, principal)).isInstanceOf(SelfBanException.class) が使える)
    // - userMapper.updateEnabled()が一切呼ばれていないこと
    //   (verify(userMapper, never()).updateEnabled(anyLong(), anyBoolean()) で確認できる。
    //    anyLong/anyBooleanはorg.mockito.ArgumentMatchersのstaticメソッド)
    @Test
    void 自分自身をBANしようとするとSelfBanException() {
        CustomUserDetails principal = principal(1L);

        assertThatThrownBy(() -> userService.ban(1L, principal)).isInstanceOf(SelfBanException.class);
        verify(userMapper, never()).updateEnabled(anyLong(), anyBoolean());
    }

    private CustomUserDetails principal(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("test-user");
        user.setPassword("password");
        user.setRole(Role.ADMIN);
        user.setEnabled(true);
        return new CustomUserDetails(user);
    }
}