package kr.kro.deom.domain.user.service;

import kr.kro.deom.common.utils.SecurityUtils;
import kr.kro.deom.domain.user.dto.OwnerMyPageResponse;
import kr.kro.deom.domain.user.entity.User;
import kr.kro.deom.domain.user.exception.UserNotFoundException;
import kr.kro.deom.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OwnerMyPageService {

    private final UserRepository userRepository;

    public OwnerMyPageResponse getOwnerMyPage() {

        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        return new OwnerMyPageResponse(user.getNickname(), user.getProvider());
    }
}
