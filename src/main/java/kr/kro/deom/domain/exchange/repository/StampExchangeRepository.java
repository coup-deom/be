package kr.kro.deom.domain.exchange.repository;

import kr.kro.deom.domain.deom.entity.Deom;
import kr.kro.deom.domain.exchange.entity.StampExchange;
import kr.kro.deom.domain.exchange.service.StampExchangeJoinProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StampExchangeRepository extends JpaRepository<StampExchange, Long> {

    @Query("SELECT new com.your.package.StampExchangeJoinProjection(e, s1, s2) " +
            "FROM StampExchange e " +
            "LEFT JOIN Store s1 ON e.sourceStoreId = s1.id " +
            "LEFT JOIN Store s2 ON e.targetStoreId = s2.id " +
            "ORDER BY e.updatedAt DESC")
    List<StampExchangeJoinProjection> findAllWithStoreInfo();

}
