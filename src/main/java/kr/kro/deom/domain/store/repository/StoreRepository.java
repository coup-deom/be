package kr.kro.deom.domain.store.repository;

import kr.kro.deom.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StoreRepository extends JpaRepository<Store, Long> {

    @Query("SELECT s.ownerId FROM Store s WHERE s.id = :storeId")
    Long findOwnerIdByStoreId(Long storeId);
}
