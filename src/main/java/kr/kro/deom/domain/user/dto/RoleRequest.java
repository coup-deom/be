package kr.kro.deom.domain.user.dto;

import kr.kro.deom.domain.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoleRequest {
    private Long userId;
    private Role role;
}
