package kr.kro.deom.domain.store.repository;

import java.util.Optional;
import kr.kro.deom.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {

    Optional<Store> findByBusinessNumberAndIsDeletedFalse(Long businessNumber);

    Optional<Store> findByStoreNameAndBranchNameAndIsDeletedFalse(
            String storeName, String branchName);
}
