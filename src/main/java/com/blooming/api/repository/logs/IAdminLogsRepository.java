package com.blooming.api.repository.logs;

import com.blooming.api.entity.AdminLog;
import com.blooming.api.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAdminLogsRepository extends JpaRepository<AdminLog, Long> {
}
