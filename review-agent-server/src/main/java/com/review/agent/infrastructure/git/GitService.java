package com.review.agent.infrastructure.git;

import com.review.agent.common.exception.BizException;
import com.review.agent.domain.entity.Project;
import com.review.agent.domain.enums.ProjectStatus;
import com.review.agent.domain.exception.CommonExceptionEnum;
import com.review.agent.infrastructure.persistence.ProjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitService {

    private final ProjectMapper projectMapper;
    private final GitOperations gitOperations;

    @Async("gitTaskExecutor")
    public void cloneRepository(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BizException(CommonExceptionEnum.PROJECT_NOT_FOUND);
        }

        updateProjectStatus(projectId, ProjectStatus.CLONING, null);

        try {
            gitOperations.cloneRepository(
                    project.getRepoUrl(),
                    project.getLocalPath(),
                    project.getDefaultBranch()
            );
            updateProjectStatus(projectId, ProjectStatus.READY, null);
            log.info("仓库克隆成功: projectId={}", projectId);
        } catch (BizException e) {
            updateProjectStatus(projectId, ProjectStatus.ERROR, e.getMessage());
            log.error("仓库克隆失败: projectId={}", projectId, e);
        }
    }

    public List<String> getBranches(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BizException(CommonExceptionEnum.PROJECT_NOT_FOUND);
        }
        if (project.getStatus() != ProjectStatus.READY) {
            throw new BizException(CommonExceptionEnum.REPO_NOT_CLONED);
        }
        return gitOperations.getBranches(project.getLocalPath());
    }

    private void updateProjectStatus(Long projectId, ProjectStatus status, String errorMessage) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            return;
        }
        project.setStatus(status);
        project.setUpdatedAt(LocalDateTime.now());
        project.setCloneErrorMessage(errorMessage);
        projectMapper.updateById(project);
    }
}