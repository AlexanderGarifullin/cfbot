package com.ga.cfbot.infrastructure.repository;

import com.ga.cfbot.domain.model.GroupMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Long> {
    List<GroupMembership> findByGroupId(Long groupId);
}
