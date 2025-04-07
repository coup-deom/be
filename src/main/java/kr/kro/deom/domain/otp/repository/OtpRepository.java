package kr.kro.deom.domain.otp.repository;

import kr.kro.deom.domain.otp.entity.OtpStatus;
import kr.kro.deom.domain.otp.entity.OtpUsage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpRepository extends CrudRepository<OtpUsage, Long> {
    OtpUsage findByOtpAndStoreIdAndStatus(Long otp, Long storeId, OtpStatus status);
    boolean existsByOtpAndStoreIdAndStatus(Long otp, Long storeId, OtpStatus status);
}
