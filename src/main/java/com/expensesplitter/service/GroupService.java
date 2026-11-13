package com.expensesplitter.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.expensesplitter.dto.GroupResponse;
import com.expensesplitter.dto.MemberResponse;
import com.expensesplitter.entity.ExpenseGroup;
import com.expensesplitter.entity.GroupMember;
import com.expensesplitter.entity.User;
import com.expensesplitter.repository.ExpenseGroupRepository;
import com.expensesplitter.repository.GroupMemberRepository;
import com.expensesplitter.repository.UserRepository;

@Service
public class GroupService {

    private final ExpenseGroupRepository groups;
    private final GroupMemberRepository members;
    private final UserRepository users;

    public GroupService(ExpenseGroupRepository groups, GroupMemberRepository members, UserRepository users) {
        this.groups = groups;
        this.members = members;
        this.users = users;
    }

    @Transactional
    public GroupResponse create(String name, Long userId) {
        ExpenseGroup group = new ExpenseGroup();
        group.setName(name.trim());
        group.setCreatedBy(userId);
        group = groups.save(group);
        members.save(new GroupMember(group.getId(), userId));
        return toResponse(group);
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> listMine(Long userId) {
        List<Long> groupIds = members.findByUserId(userId).stream()
                .map(GroupMember::getGroupId)
                .toList();
        return groups.findAllById(groupIds).stream()
                .sorted(Comparator.comparing(ExpenseGroup::getId).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public GroupResponse get(Long groupId, Long userId) {
        ExpenseGroup group = find(groupId);
        ensureMember(groupId, userId);
        return toResponse(group);
    }

    @Transactional
    public GroupResponse addMember(Long groupId, String email, Long requesterId) {
        ExpenseGroup group = find(groupId);
        ensureMember(groupId, requesterId);
        ensureCreator(group, requesterId);

        User user = users.findByEmail(email.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No user with that email. Ask them to register first."));
        if (members.existsByGroupIdAndUserId(groupId, user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already a member of this group");
        }
        members.save(new GroupMember(groupId, user.getId()));
        return toResponse(group);
    }

    @Transactional
    public GroupResponse removeMember(Long groupId, Long memberUserId, Long requesterId) {
        ExpenseGroup group = find(groupId);
        ensureMember(groupId, requesterId);
        ensureCreator(group, requesterId);

        if (memberUserId.equals(group.getCreatedBy())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The group creator cannot be removed");
        }
        if (!members.existsByGroupIdAndUserId(groupId, memberUserId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "That user is not in this group");
        }
        members.deleteByGroupIdAndUserId(groupId, memberUserId);
        return toResponse(group);
    }

    @Transactional
    public void delete(Long groupId, Long requesterId) {
        ExpenseGroup group = find(groupId);
        ensureMember(groupId, requesterId);
        ensureCreator(group, requesterId);
        members.deleteByGroupId(groupId);
        groups.delete(group);
    }

    /** Used by the expense code on later days: only members may see or use a group. */
    public void ensureMember(Long groupId, Long userId) {
        if (!members.existsByGroupIdAndUserId(groupId, userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found");
        }
    }

    private ExpenseGroup find(Long groupId) {
        return groups.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));
    }

    private void ensureCreator(ExpenseGroup group, Long userId) {
        if (!group.getCreatedBy().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the group creator can do this");
        }
    }

    private GroupResponse toResponse(ExpenseGroup group) {
        List<GroupMember> groupMembers = members.findByGroupId(group.getId());
        Map<Long, User> usersById = users
                .findAllById(groupMembers.stream().map(GroupMember::getUserId).toList())
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        List<MemberResponse> memberList = groupMembers.stream()
                .map(m -> usersById.get(m.getUserId()))
                .filter(Objects::nonNull)
                .map(u -> new MemberResponse(u.getId(), u.getName(), u.getEmail()))
                .toList();
        return new GroupResponse(group.getId(), group.getName(), group.getCreatedBy(),
                group.getCreatedAt(), memberList);
    }
}