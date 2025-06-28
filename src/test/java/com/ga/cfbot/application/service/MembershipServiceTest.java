package com.ga.cfbot.application.service;

import com.ga.cfbot.domain.model.GroupMembership;
import com.ga.cfbot.infrastructure.repository.GroupMembershipRepository;
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
class MembershipServiceTest {

    @Mock
    private GroupMembershipRepository repo;

    @InjectMocks
    private MembershipService service;

    private GroupMembership exampleMembership;

    @BeforeEach
    void setUp() {
        exampleMembership = GroupMembership.builder()
                .id(7L)
                .groupId(42L)
                .membershipCodeforcesName("tester")
                .build();
    }

    @Test
    void add_ShouldSaveAndReturnMembership() {
        // arrange: repo.save returns membership with id set
        when(repo.save(any(GroupMembership.class)))
                .thenAnswer(invocation -> {
                    GroupMembership m = invocation.getArgument(0);
                    m.setId(7L);
                    return m;
                });

        // act
        GroupMembership result = service.add(42L, "tester");

        // assert
        assertThat(result.getId()).isEqualTo(7L);
        assertThat(result.getGroupId()).isEqualTo(42L);
        assertThat(result.getMembershipCodeforcesName()).isEqualTo("tester");
        verify(repo).save(any(GroupMembership.class));
    }

    @Test
    void list_ShouldDelegateToRepository() {
        // arrange
        List<GroupMembership> list = List.of(
                GroupMembership.builder().id(1L).groupId(5L).membershipCodeforcesName("a").build(),
                GroupMembership.builder().id(2L).groupId(5L).membershipCodeforcesName("b").build()
        );
        when(repo.findByGroupId(5L)).thenReturn(list);

        // act
        List<GroupMembership> result = service.list(5L);

        // assert
        assertThat(result).containsExactlyElementsOf(list);
        verify(repo).findByGroupId(5L);
    }

    @Test
    void remove_ShouldDelegateToRepository() {
        // act
        service.remove(13L);

        // assert
        verify(repo).deleteById(13L);
    }
}
