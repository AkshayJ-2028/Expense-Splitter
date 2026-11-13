package com.expensesplitter.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.expensesplitter.dto.AddMemberRequest;
import com.expensesplitter.dto.GroupRequest;
import com.expensesplitter.dto.GroupResponse;
import com.expensesplitter.security.CurrentUser;
import com.expensesplitter.service.GroupService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;
    private final CurrentUser currentUser;

    public GroupController(GroupService groupService, CurrentUser currentUser) {
        this.groupService = groupService;
        this.currentUser = currentUser;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupResponse create(@Valid @RequestBody GroupRequest request) {
        return groupService.create(request.name(), currentUser.id());
    }

    @GetMapping
    public List<GroupResponse> list() {
        return groupService.listMine(currentUser.id());
    }

    @GetMapping("/{id}")
    public GroupResponse get(@PathVariable Long id) {
        return groupService.get(id, currentUser.id());
    }

    @PostMapping("/{id}/members")
    public GroupResponse addMember(@PathVariable Long id, @Valid @RequestBody AddMemberRequest request) {
        return groupService.addMember(id, request.email(), currentUser.id());
    }

    @DeleteMapping("/{id}/members/{userId}")
    public GroupResponse removeMember(@PathVariable Long id, @PathVariable Long userId) {
        return groupService.removeMember(id, userId, currentUser.id());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        groupService.delete(id, currentUser.id());
    }
}