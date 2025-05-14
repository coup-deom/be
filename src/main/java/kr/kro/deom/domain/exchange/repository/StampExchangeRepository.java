package kr.kro.deom.domain.exchange.repository;

import java.util.List;
import kr.kro.deom.domain.exchange.entity.StampExchange;
import kr.kro.deom.domain.exchange.service.StampExchangeJoinProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface StampExchangeRepository extends JpaRepository<StampExchange, Long> {

    @Query(
            """
    SELECT new kr.kro.deom.domain.exchange.service.StampExchangeJoinProjection(e, s1, s2)
    FROM StampExchange e
    LEFT JOIN Store s1 ON e.sourceStoreId = s1.id
    LEFT JOIN Store s2 ON e.targetStoreId = s2.id
    WHERE e.status = 'PENDING'
    ORDER BY e.updatedAt DESC
""")
    List<StampExchangeJoinProjection> findPendingAllExchanges();

    @Query(
            "SELECT new kr.kro.deom.domain.exchange.service.StampExchangeJoinProjection(e, s1, s2) "
                    + "FROM StampExchange e "
                    + "LEFT JOIN Store s1 ON e.sourceStoreId = s1.id "
                    + "LEFT JOIN Store s2 ON e.targetStoreId = s2.id "
                    + "WHERE e.sourceStoreId IN :storeIds AND e.status = 'PENDING'"
                    + "ORDER BY e.updatedAt DESC")
    List<StampExchangeJoinProjection> findBySourceStoreIdInWithStoreInfo(List<Long> storeIds);

    @Modifying
    @Query(
            "UPDATE StampExchange e SET e.status = 'COMPLETED', e.responderId = :userId "
                    + "WHERE e.id = :id AND e.status = 'PENDING'")
    int updateStatusIfPending(Long id, Long userId);

    @Query(
            """
    SELECT new kr.kro.deom.domain.exchange.service.StampExchangeJoinProjection(e, s1, s2)
    FROM StampExchange e
    LEFT JOIN Store s1 ON e.sourceStoreId = s1.id
    LEFT JOIN Store s2 ON e.targetStoreId = s2.id
    WHERE e.sourceStoreId = :storeId OR e.targetStoreId = :storeId
    ORDER BY e.updatedAt DESC
""")
    List<StampExchangeJoinProjection> findAllExchanges(Long storeId);
}
