package kr.kro.deom.domain.deom.repository;

import java.util.List;
import kr.kro.deom.domain.deom.entity.Deom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeomRepository extends JpaRepository<Deom, Long> {
  boolean existsByStoreId(Long id);

  List<Deom> findByStoreIdOrderByRequiredStampAmountAsc(Long id);
}
