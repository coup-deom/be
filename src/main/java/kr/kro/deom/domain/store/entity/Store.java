package kr.kro.deom.domain.store.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "store")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK: 업주 회원 ID
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "business_number", nullable = false)
    private Long businessNumber;

    @Column(name = "store_name", nullable = false)
    private String storeName;

    @Column(name = "branch_name")
    private String branchName;

    @Column(name = "address_city", nullable = false)
    private String addressCity;

    @Column(name = "address_street", nullable = false)
    private String addressStreet;

    @Column(name = "address_detail")
    private String addressDetail;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at", nullable = false)
    private LocalDateTime deletedAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "image")
    private String image;
}
