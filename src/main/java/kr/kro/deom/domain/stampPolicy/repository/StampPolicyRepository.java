package kr.kro.deom.domain.stampPolicy.repository;

import java.util.List;
import java.util.Optional;
import kr.kro.deom.domain.stampPolicy.dto.StampPolicyDto;
import kr.kro.deom.domain.stampPolicy.entity.StampPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StampPolicyRepository extends JpaRepository<StampPolicy, Long> {

    @Query(
            "SELECT new kr.kro.deom.domain.stampPolicy.dto.StampPolicyDto(sp.id, sp.baseAmount, sp.stampCount) "
                    + "FROM StampPolicy sp WHERE sp.storeId = :storeId AND sp.deletedAt IS NULL ORDER BY sp.baseAmount")
    List<StampPolicyDto> findPoliciesByStoreId(@Param("storeId") long storeId);

    Optional<StampPolicy> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByStoreIdAndBaseAmountAndDeletedAtIsNull(Long storeId, Integer baseAmount);
}
