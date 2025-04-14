package com.blooming.api.repository.evaluation;

import com.blooming.api.entity.Evaluation;
import com.blooming.api.entity.Nursery;
import com.blooming.api.entity.Pot;
import com.blooming.api.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface IEvaluationRepository extends JpaRepository<Evaluation, Long> {
    Optional<Evaluation> findByNursery(Nursery nursery);

    Optional<Evaluation> findByPot(Pot pot);

    Page<Evaluation> findByNurseryAndStatus(Nursery nursery, boolean status, Pageable pageable);

    Page<Evaluation> findByPotAndStatus(Pot pot, boolean status, Pageable pageable);

    Page<Evaluation> findByUserAndStatus(User user, boolean status, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Evaluation n SET n.status = true WHERE n.id = :evaluationId")
    int activate(@Param("evaluationId") Long evaluationId);

    @Modifying
    @Transactional
    @Query("UPDATE Evaluation n SET n.status = false WHERE n.id = :evaluationId")
    int deactivate(@Param("evaluationId") Long evaluationId);
}
