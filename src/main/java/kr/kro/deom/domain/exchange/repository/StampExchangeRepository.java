package kr.kro.deom.domain.exchange.repository;

import java.util.List;
import kr.kro.deom.domain.exchange.entity.StampExchange;
import kr.kro.deom.domain.exchange.service.StampExchangeJoinProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StampExchangeRepository extends JpaRepository<StampExchange, Long> {

    @Query(
            "SELECT new kr.kro.deom.domain.exchange.service.StampExchangeJoinProjection(e, s1, s2) "
                    + "FROM StampExchange e "
                    + "LEFT JOIN Store s1 ON e.sourceStoreId = s1.id "
                    + "LEFT JOIN Store s2 ON e.targetStoreId = s2.id "
                    + "ORDER BY e.updatedAt DESC")
    List<StampExchangeJoinProjection> findAllWithStoreInfo();

    @Query(
            "SELECT new kr.kro.deom.domain.exchange.service.StampExchangeJoinProjection(e, s1, s2) "
                    + "FROM StampExchange e "
                    + "LEFT JOIN Store s1 ON e.sourceStoreId = s1.id "
                    + "LEFT JOIN Store s2 ON e.targetStoreId = s2.id "
                    + "WHERE e.sourceStoreId IN :storeIds "
                    + "ORDER BY e.updatedAt DESC")
    List<StampExchangeJoinProjection> findBySourceStoreIdInWithStoreInfo(List<Long> storeIds);
}
