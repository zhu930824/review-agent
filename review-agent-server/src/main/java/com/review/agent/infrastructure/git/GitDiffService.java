package com.review.agent.infrastructure.git;

import com.review.agent.common.exception.BizException;
import com.review.agent.domain.dto.diff.FileChange;
import com.review.agent.domain.entity.Project;
import com.review.agent.domain.exception.CommonExceptionEnum;
import com.review.agent.infrastructure.persistence.ProjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GitDiffService {

    private final GitOperations gitOperations;
    private final DiffParser diffParser;
    private final ProjectMapper projectMapper;
    private final GitLabDiffService gitLabDiffService;

    public List<FileChange> getBranchDiff(Long projectId, String sourceBranch, String targetBranch) {
        if (gitLabDiffService.isGitLabConfigured(projectId)) {
            return gitLabDiffService.getBranchDiff(projectId, sourceBranch, targetBranch);
        }
        String localPath = resolveLocalPath(projectId);
        gitOperations.fetchRepository(localPath);
        String diffOutput = gitOperations.getDiff(localPath, "origin/" + sourceBranch, "origin/" + targetBranch);
        return diffParser.parse(diffOutput);
    }

    public List<FileChange> getCommitDiff(Long projectId, String sourceCommit, String targetCommit) {
        if (gitLabDiffService.isGitLabConfigured(projectId)) {
            return gitLabDiffService.getCommitDiff(projectId, sourceCommit, targetCommit);
        }
        String localPath = resolveLocalPath(projectId);
        String diffOutput = gitOperations.getDiff(localPath, sourceCommit, targetCommit);
        return diffParser.parse(diffOutput);
    }

    public String getLatestCommit(Long projectId, String branch) {
        if (gitLabDiffService.isGitLabConfigured(projectId)) {
            return gitLabDiffService.getLatestCommit(projectId, branch);
        }
        String localPath = resolveLocalPath(projectId);
        gitOperations.fetchRepository(localPath);
        return gitOperations.getLatestCommit(localPath, "origin/" + branch);
    }

    public List<FileChange> getDiffWithContext(Long projectId, String sourceBranch, String targetBranch, int contextLines) {
        return getBranchDiff(projectId, sourceBranch, targetBranch);
    }

    private String resolveLocalPath(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BizException(CommonExceptionEnum.PROJECT_NOT_FOUND);
        }
        if (project.getLocalPath() == null) {
            throw new BizException(CommonExceptionEnum.REPO_NOT_CLONED);
        }
        return project.getLocalPath();
    }
}