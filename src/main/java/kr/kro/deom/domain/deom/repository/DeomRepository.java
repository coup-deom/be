package kr.kro.deom.domain.deom.repository;

import java.util.List;
import kr.kro.deom.domain.deom.dto.DeomDto;
import kr.kro.deom.domain.deom.entity.Deom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeomRepository extends JpaRepository<Deom, Long> {
    boolean existsByStoreId(Long id);

    List<Deom> findByStoreIdOrderByRequiredStampAmountAsc(Long id);

    @Query(
            "SELECT new kr.kro.deom.domain.deom.dto.DeomDto(d.id, d.name, d.requiredStampAmount) "
                    + "FROM Deom d WHERE d.storeId = :storeId AND d.deletedAt IS NULL ORDER BY d.requiredStampAmount")
    List<DeomDto> findPoliciesByStoreId(@Param("storeId") long storeId);

    boolean existsByStoreIdAndName(Long storeId, String name);
}
