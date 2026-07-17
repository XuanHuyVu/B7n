package com.huy.b7n.repository;

import com.huy.b7n.common.ERoundStatus;
import com.huy.b7n.entity.RoundEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoundRepository extends JpaRepository<RoundEntity, Long> {

    @EntityGraph(attributePaths = {"session"})
    Optional<RoundEntity> findTopBySession_SessionCodeOrderByRoundNumberDesc(String sessionCode);

    @EntityGraph(attributePaths = {"session"})
    Optional<RoundEntity> findTopBySession_SessionCodeAndStatusInOrderByRoundNumberDesc(
            String sessionCode,
            Collection<ERoundStatus> statuses
    );

    @EntityGraph(attributePaths = {"session"})
    Optional<RoundEntity> findBySession_SessionCodeAndRoundNumber(String sessionCode, Integer roundNumber);

    @EntityGraph(attributePaths = {"session"})
    List<RoundEntity> findAllBySession_SessionCodeOrderByRoundNumberAsc(String sessionCode);

    void deleteBySession_SessionCode(String sessionCode);
}