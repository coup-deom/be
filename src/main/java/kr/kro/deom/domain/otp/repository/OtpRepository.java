package kr.kro.deom.domain.otp.repository;

import java.util.Map;
import java.util.Optional;
import kr.kro.deom.domain.otp.entity.OtpStatus;
import kr.kro.deom.domain.otp.entity.OtpUsage;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpRepository extends CrudRepository<OtpUsage, Long> {
    OtpUsage findByOtpAndStoreIdAndStatus(Long otp, Long storeId, OtpStatus status);

    boolean existsByOtpAndStoreIdAndStatus(Long otp, Long storeId, OtpStatus status);

    @Query(
            "SELECT o.id FROM OtpUsage o WHERE o.otp = :otp AND o.storeId =:storedId AND o.status =:status")
    Optional<Long> findIDByOtpAndStoreIdAndStatus(Long otp, Long storeId, OtpStatus status);

    @Query(
            "SELECT new map(o.id as id, o.type as type, o.status as status) FROM OtpUsage o WHERE o.otp =:otp AND o.storeId =:storedId AND o.status =:status")
    Optional<Map<String, Object>> findOtpInfoByOtpAndStoreIdAndStatus(
            Long otp, Long storeId, OtpStatus status);
}
