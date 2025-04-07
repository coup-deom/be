package kr.kro.deom.domain.user.repository;

import java.util.Optional;
import kr.kro.deom.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserBySocialId(String socialId);
}
