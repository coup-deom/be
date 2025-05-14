package kr.kro.deom.domain.user.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.Optional;
import kr.kro.deom.common.security.oauth.OAuth2Provider;
import kr.kro.deom.common.security.oauth.OAuth2UserInfo;
import kr.kro.deom.domain.user.dto.UserResponse;
import kr.kro.deom.domain.user.entity.Role;
import kr.kro.deom.domain.user.entity.User;
import kr.kro.deom.domain.user.exception.UserNotFoundException;
import kr.kro.deom.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private UserService userService;

    private User user;
    private OAuth2UserInfo oAuth2UserInfo;

    @BeforeEach
    void setUp() {
        user =
                User.builder()
                        .id(1L)
                        .email("test@example.com")
                        .nickname("테스트유저")
                        .socialId("social123")
                        .provider(OAuth2Provider.GOOGLE)
                        .role(Role.PENDING)
                        .deleted(false)
                        .build();

        oAuth2UserInfo =
                new OAuth2UserInfo() {
                    @Override
                    public OAuth2Provider getProvider() {
                        return OAuth2Provider.GOOGLE;
                    }

                    @Override
                    public String getProviderId() {
                        return "social123";
                    }

                    @Override
                    public String getEmail() {
                        return "test@example.com";
                    }

                    @Override
                    public String getName() {
                        return "테스트유저";
                    }
                };
    }

    @Test
    @DisplayName("유저 조회 성공")
    void getUser_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUser(1L);

        assertThat(result).isEqualTo(user);
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("유저 조회 실패 - 존재하지 않음")
    void getUser_fail() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(1L)).isInstanceOf(UserNotFoundException.class);

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("유저 정보 조회")
    void getUserInfo_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserInfo(1L);

        assertThat(response.getEmail()).isEqualTo(user.getEmail());
        assertThat(response.getNickname()).isEqualTo(user.getNickname());

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("OAuth 로그인 - 기존 유저 존재")
    void findOrCreateUser_existingUser() {
        user.updateDeleted(true);

        when(userRepository.findUserBySocialId(oAuth2UserInfo.getProviderId()))
                .thenReturn(Optional.of(user));

        User result = userService.findOrCreateUser(oAuth2UserInfo);

        assertThat(result.isDeleted()).isFalse();
        verify(userRepository, times(1)).findUserBySocialId(oAuth2UserInfo.getProviderId());
    }

    @Test
    @DisplayName("OAuth 로그인 - 신규 유저 생성")
    void findOrCreateUser_newUser() {
        when(userRepository.findUserBySocialId(oAuth2UserInfo.getProviderId()))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.findOrCreateUser(oAuth2UserInfo);

        assertThat(result.getSocialId()).isEqualTo(oAuth2UserInfo.getProviderId());
        assertThat(result.getRole()).isEqualTo(Role.PENDING);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("유저 삭제")
    void deleteUser_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        assertThat(user.isDeleted()).isTrue();
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("유저 유효성 검증 성공")
    void validateUser_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.validateUserByUserId(1L);

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("유저 유효성 검증 실패")
    void validateUser_fail() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.validateUserByUserId(1L))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, times(1)).findById(1L);
    }
}
