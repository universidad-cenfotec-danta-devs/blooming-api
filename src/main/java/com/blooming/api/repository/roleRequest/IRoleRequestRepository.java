package com.blooming.api.repository.roleRequest;

import com.blooming.api.entity.RoleRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IRoleRequestRepository extends JpaRepository<RoleRequest, Long> {
}
