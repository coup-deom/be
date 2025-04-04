package kr.kro.deom.domain.myStamp.repository;

import kr.kro.deom.domain.myStamp.entity.MyStamp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyStampRepository extends JpaRepository<MyStamp, Long> {
}

