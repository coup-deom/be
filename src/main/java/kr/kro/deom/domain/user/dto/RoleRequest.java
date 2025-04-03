package kr.kro.deom.domain.user.dto;

import kr.kro.deom.domain.user.entity.Role;
import lombok.Getter;

@Getter
public class RoleRequest {
  private Long userId;
  private Role role;
}
