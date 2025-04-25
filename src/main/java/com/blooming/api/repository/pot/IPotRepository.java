package com.blooming.api.repository.pot;

import com.blooming.api.entity.Pot;
import com.blooming.api.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface IPotRepository extends JpaRepository<Pot, Long> {
    Page<Pot> findByStatus(boolean status, Pageable pageable);

    Page<Pot> findByDesignerAndStatus(User user, boolean status, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Pot n SET n.status = true WHERE n.id = :potId")
    int activate(@Param("potId") Long potId);

    @Modifying
    @Transactional
    @Query("UPDATE Pot n SET n.status = false WHERE n.id = :potId")
    int deactivate(@Param("potId") Long potId);
}
