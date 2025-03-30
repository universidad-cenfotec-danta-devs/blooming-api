package com.blooming.api.repository.canton;

import com.blooming.api.entity.Canton;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ICantonRepository extends JpaRepository<Canton, Long> {
    Optional<Canton> findByName(String canton);
}
