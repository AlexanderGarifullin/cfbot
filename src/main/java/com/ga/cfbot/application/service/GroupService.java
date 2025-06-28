package com.ga.cfbot.application.service;

import com.ga.cfbot.domain.model.Group;
import com.ga.cfbot.infrastructure.repository.GroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class GroupService {
    private final GroupRepository repo;
    public GroupService(GroupRepository repo) { this.repo = repo; }

    @Transactional
    public Group create(String name, Long owner) {
        Group g = new Group(); g.setName(name); g.setOwner(owner);
        return repo.save(g);
    }

    public List<Group> list(Long owner) {
        return repo.findByOwnerOrderByNameAsc(owner);
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
