package com.review.agent.service;

import com.review.agent.domain.dto.ProjectMemberVO;
import com.review.agent.domain.dto.UpsertProjectMemberRequest;
import com.review.agent.infrastructure.auth.ProjectPermission;

import java.util.List;

public interface ProjectAccessService {

    void require(Long projectId, ProjectPermission permission);

    boolean hasGlobalProjectAccess();

    List<Long> listAccessibleProjectIds();

    void addCurrentUserAsOwner(Long projectId);

    List<ProjectMemberVO> listMembers(Long projectId);

    ProjectMemberVO upsertMember(Long projectId, UpsertProjectMemberRequest request);

    void removeMember(Long projectId, Long userId);
}
