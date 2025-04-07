package kr.kro.deom.domain.otp.repository;

import java.util.Map;
import java.util.Optional;
import kr.kro.deom.domain.otp.entity.OtpUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OtpUsageRepository extends JpaRepository<OtpUsage, Long> {

    @Query("SELECT o.id FROM OtpUsage o WHERE o.otp = :otp")
    Optional<Long> findIdByOtp(Long otp);

    @Query(
            "SELECT new map(o.id as id, o.type as type, o.status as status) FROM OtpUsage o WHERE o.otp =:otp")
    Optional<Map<String, Object>> findIdAndTypeAndStatusByOtp(Long otp);
}
