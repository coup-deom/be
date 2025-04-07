package kr.kro.deom.domain.otp.repository;

import kr.kro.deom.domain.otp.entity.OtpStatus;
import kr.kro.deom.domain.otp.entity.OtpUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpRepository extends JpaRepository<OtpUsage, Long> {

  boolean existsByOtpAndStoreIdAndStatus(Long otp, Long storeId, OtpStatus status);
}
