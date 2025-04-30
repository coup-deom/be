package kr.kro.deom.domain.user.service;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kr.kro.deom.common.security.oauth.OAuth2Provider;
import kr.kro.deom.common.utils.SecurityUtils;
import kr.kro.deom.domain.user.dto.OwnerMyPageResponse;
import kr.kro.deom.domain.user.entity.Role;
import kr.kro.deom.domain.user.entity.User;
import kr.kro.deom.domain.user.exception.UserNotFoundException;
import kr.kro.deom.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OwnerMyPageServiceTest {

    @InjectMocks private OwnerMyPageService ownerMyPageService;

    @Mock private UserRepository userRepository;

    @Test
    @DisplayName("사장 마이페이지 기본 정보 조회 성공")
    public void getOwnerMypage_Success() {
        // given
        Long mockUserId = 1L;

        try (MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {
            mockedStatic.when(SecurityUtils::getCurrentUserId).thenReturn(mockUserId);

            User user =
                    User.builder()
                            .id(mockUserId)
                            .socialId("socialId")
                            .provider(OAuth2Provider.KAKAO)
                            .email("test@test.com")
                            .nickname("테스트닉네임")
                            .role(Role.OWNER)
                            .deleted(false)
                            .build();

            when(userRepository.findById(mockUserId)).thenReturn(Optional.of(user));

            // when
            OwnerMyPageResponse response = ownerMyPageService.getOwnerMyPage();

            // then
            assertNotNull(response);
            assertEquals("테스트닉네임", response.nickname());
            assertEquals(OAuth2Provider.KAKAO, response.provider());
        }
    }

    @Test
    @DisplayName("사장 마이페이지 기본 정보 조회 실패")
    public void getOwnerMypage_userNotFound() {

        // given
        Long mockUserId = 1L;

        try (MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {
            mockedStatic.when(SecurityUtils::getCurrentUserId).thenReturn(mockUserId);

            when(userRepository.findById(mockUserId)).thenReturn(Optional.empty());

            // when , then
            assertThrows(
                    UserNotFoundException.class,
                    () -> {
                        ownerMyPageService.getOwnerMyPage();
                    });
        }
    }
}
