package kr.kro.deom.domain.myStamp.repository;

import io.lettuce.core.dynamic.annotation.Param;
import java.util.Optional;
import kr.kro.deom.domain.myStamp.entity.MyStamp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MyStampRepository extends JpaRepository<MyStamp, Long> {
    boolean existsByStoreId(Long id);

    @Query(
            "SELECT ms.stampAmount FROM MyStamp ms WHERE ms.userId = :userId AND ms.storeId = :storeId")
    Integer findStampAmountByUserIdAndStoreId(
            @Param("userId") Long userId, @Param("storeId") Long storeId);

    @Modifying
    @Query(
            "UPDATE MyStamp ms SET ms.stampAmount = ms.stampAmount - :usedStampAmount WHERE ms.userId = :userId AND ms.storeId = :storeId")
    void updateStampAmount(
            @Param("userId") Long userId,
            @Param("storeId") Long storeId,
            @Param("usedStampAmount") Integer usedStampAmount);

    @Modifying
    @Query(
            value =
                    "UPDATE my_stamp "
                            + "SET stamp_amount = stamp_amount + :amount "
                            + "WHERE user_id = :userId "
                            + "AND store_id = :storeId "
                            + "RETURNING id, stamp_count, updated_at",
            nativeQuery = true)
    Optional<MyStamp> incrementStamp(
            @Param("userId") Long userId,
            @Param("storeId") Long storeId,
            @Param("amount") int amount);
}
