package kr.kro.deom.domain.user.entity;

import jakarta.persistence.*;
import kr.kro.deom.common.security.oauth.OAuth2Provider;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String socialId;

  @Enumerated(EnumType.STRING)
  private OAuth2Provider provider;

  private String email;

  private String nickname;

  @Enumerated(EnumType.STRING)
  private Role role;

  private boolean deleted;

  public void updateRole(Role newRole) {
    this.role = newRole;
  }

  public void updateDeleted(boolean deleted) {
    this.deleted = deleted;
  }
}
