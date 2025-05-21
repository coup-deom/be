package kr.kro.deom.domain.myStamp.repository;

import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import kr.kro.deom.domain.myStamp.dto.UserAccumulatedStampsDto;
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
    Integer updateStampAmount(
            @Param("userId") Long userId,
            @Param("storeId") Long storeId,
            @Param("usedStampAmount") Integer usedStampAmount);

    @Modifying
    @Query(
            "UPDATE MyStamp ms SET ms.stampAmount = ms.stampAmount + :stampAmount WHERE ms.userId = :userId AND ms.storeId = :storeId")
    Integer addStampAmount(
            @Param("userId") Long userId,
            @Param("storeId") Long storeId,
            @Param("stampAmount") Integer stampAmount);

    @Modifying
    @Query(
            "UPDATE MyStamp ms SET ms.stampAmount = ms.stampAmount + :amount, ms.accumulatedStampAmount = ms.accumulatedStampAmount + :amount WHERE ms.userId = :userId AND ms.storeId = :storeId")
    Integer incrementStamp(
            @Param("userId") Long userId,
            @Param("storeId") Long storeId,
            @Param("amount") int amount);

    @Query("SELECT ms.storeId FROM MyStamp ms WHERE ms.userId = :userId AND ms.stampAmount >= 0")
    List<Long> findStoreIdsByUserIdWithStamps(@Param("userId") Long userId);

    // 가게별 스탬프 수량 확인용
    @Query("SELECT ms FROM MyStamp ms WHERE ms.userId = :userId AND ms.stampAmount > 0")
    List<MyStamp> findAllByUserIdWithStamps(@Param("userId") Long userId);

    @Modifying
    @Query(
            "UPDATE MyStamp ms SET ms.stampAmount = ms.stampAmount - :amount "
                    + "WHERE ms.userId = :userId AND ms.storeId = :storeId AND ms.stampAmount >= :amount")
    int deductStampAmountIfSufficient(
            @Param("userId") Long userId,
            @Param("storeId") Long storeId,
            @Param("amount") int amount);

    @Modifying
    @Query(
            value =
                    "INSERT INTO my_stamp (user_id, store_id, stamp_amount, created_at, updated_at) "
                            + "VALUES (:userId, :storeId, :amount, NOW(), NOW()) "
                            + "ON DUPLICATE KEY UPDATE stamp_amount = stamp_amount + :amount, updated_at = NOW()",
            nativeQuery = true)
    int createOrIncrementStamp(
            @Param("userId") Long userId,
            @Param("storeId") Long storeId,
            @Param("amount") int amount);

    @Query(
            "SELECT new kr.kro.deom.domain.myStamp.dto.UserAccumulatedStampsDto("
                    + "ms.userId, ms.accumulatedStampAmount) "
                    + "FROM MyStamp ms "
                    + "WHERE ms.storeId = :storeId "
                    + "ORDER BY ms.accumulatedStampAmount DESC")
    List<UserAccumulatedStampsDto> findUserAccumulatedByStoreId(@Param("storeId") Long storeId);
}
