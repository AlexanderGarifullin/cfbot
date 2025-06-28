package com.ga.cfbot.infrastructure.repository;

import com.ga.cfbot.domain.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByOwner(Long owner);

    List<Group> findByOwnerOrderByNameAsc(Long owner);
}
