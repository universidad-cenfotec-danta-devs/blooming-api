package com.blooming.api.repository.nursery;

import com.blooming.api.entity.Nursery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface INurseryRepository extends JpaRepository<Nursery, Long> {

}
