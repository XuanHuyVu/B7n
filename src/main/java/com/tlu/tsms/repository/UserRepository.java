package com.tlu.tsms.repository;

import com.tlu.tsms.entity.UserEntity;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    boolean existsByUserCodeAndDeletedAtIsNull(String userCode);

    @Query("SELECT u FROM UserEntity u WHERE u.deletedAt IS NULL")
    Page<UserEntity> findAllActive(Pageable pageable);

    @Query("""
                SELECT u
                FROM UserEntity u
                WHERE u.deletedAt IS NULL
                  AND (
                      :q IS NULL
                      OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%'))
                      OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%'))
                      OR LOWER(u.userCode) LIKE LOWER(CONCAT('%', :q, '%'))
                      OR LOWER(u.role) LIKE LOWER(CONCAT('%', :q, '%'))
                  )
            """)
    Page<UserEntity> searchActive(@Param("q") String q, Pageable pageable);

    @NullMarked
    Optional<UserEntity> findById(Long id);

    @Query("SELECT u FROM UserEntity u WHERE u.deletedAt IS NOT NULL")
    Page<UserEntity> findAllDeleted(Pageable pageable);

    void deleteById(Long id);
}
