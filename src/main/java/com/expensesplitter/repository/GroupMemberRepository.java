package com.expensesplitter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.expensesplitter.entity.GroupMember;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByUserId(Long userId);
    List<GroupMember> findByGroupId(Long groupId);
    boolean existsByGroupIdAndUserId(Long groupId, Long userId);
    void deleteByGroupIdAndUserId(Long groupId, Long userId);
    void deleteByGroupId(Long groupId);
}