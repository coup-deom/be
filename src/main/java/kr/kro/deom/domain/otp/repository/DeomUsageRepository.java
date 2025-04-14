package kr.kro.deom.domain.otp.repository;

import kr.kro.deom.domain.otp.entity.DeomUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeomUsageRepository extends JpaRepository<DeomUsage, Long> {}
