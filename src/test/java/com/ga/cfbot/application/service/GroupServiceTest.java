package com.ga.cfbot.application.service;

import com.ga.cfbot.domain.model.Group;
import com.ga.cfbot.infrastructure.repository.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository repo;

    @InjectMocks
    private GroupService service;

    private Group exampleGroup;

    @BeforeEach
    void setUp() {
        exampleGroup = Group.builder()
                .id(42L)
                .name("TestGroup")
                .owner(123L)
                .build();
    }

    @Test
    void create_ShouldSaveAndReturnGroup() {
        when(repo.save(any(Group.class))).thenAnswer(invocation -> {
            Group g = invocation.getArgument(0);
            g.setId(1L);
            return g;
        });

        Group result = service.create("MyGroup", 999L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("MyGroup");
        assertThat(result.getOwner()).isEqualTo(999L);
        verify(repo).save(any(Group.class));
    }

    @Test
    void list_ShouldDelegateToRepository() {
        List<Group> groups = List.of(
                new Group(1L, "A", 1L),
                new Group(2L, "B", 1L)
        );
        when(repo.findByOwnerOrderByNameAsc(1L)).thenReturn(groups);

        List<Group> result = service.list(1L);

        assertThat(result).containsExactlyElementsOf(groups);
        verify(repo).findByOwnerOrderByNameAsc(1L);
    }

    @Test
    void delete_ShouldDelegateToRepository() {
        service.delete(123L);

        verify(repo).deleteById(123L);
    }
}
