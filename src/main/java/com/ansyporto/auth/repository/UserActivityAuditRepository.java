package com.ansyporto.auth.repository;

import com.ansyporto.auth.entity.UserActivityAudit;
import org.springframework.data.repository.CrudRepository;

import java.time.Instant;
import java.util.UUID;

public interface UserActivityAuditRepository extends CrudRepository<UserActivityAudit, UUID> {
    long countByIpAddressAndActivityTypeAndActivityTimeAfter(String ipAddress, String activityType, Instant after);
}
