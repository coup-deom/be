package kr.kro.deom.domain.store.repository;

import kr.kro.deom.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {
}

