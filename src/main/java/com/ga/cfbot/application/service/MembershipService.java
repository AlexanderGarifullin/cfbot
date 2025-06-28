package com.ga.cfbot.application.service;

import com.ga.cfbot.domain.model.GroupMembership;
import com.ga.cfbot.infrastructure.repository.GroupMembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipService {
    private final GroupMembershipRepository repo;

    @Transactional
    public GroupMembership add(Long groupId, String cfHandle) {
        GroupMembership m = GroupMembership.builder()
                .groupId(groupId)
                .membershipCodeforcesName(cfHandle)
                .build();
        return repo.save(m);
    }

    public List<GroupMembership> list(Long groupId) {
        return repo.findByGroupId(groupId);
    }

    @Transactional
    public void remove(Long membershipId) {
        repo.deleteById(membershipId);
    }
}