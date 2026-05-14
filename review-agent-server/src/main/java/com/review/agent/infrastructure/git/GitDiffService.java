package com.review.agent.infrastructure.git;

import com.review.agent.domain.dto.diff.FileChange;
import com.review.agent.domain.entity.Project;
import com.review.agent.domain.exception.BusinessException;
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

    public List<FileChange> getBranchDiff(Long projectId, String sourceBranch, String targetBranch) {
        String localPath = resolveLocalPath(projectId);
        gitOperations.fetchRepository(localPath);
        String diffOutput = gitOperations.getDiff(localPath, sourceBranch, targetBranch);
        return diffParser.parse(diffOutput);
    }

    public List<FileChange> getCommitDiff(Long projectId, String sourceCommit, String targetCommit) {
        String localPath = resolveLocalPath(projectId);
        String diffOutput = gitOperations.getDiff(localPath, sourceCommit, targetCommit);
        return diffParser.parse(diffOutput);
    }

    public String getLatestCommit(Long projectId, String branch) {
        String localPath = resolveLocalPath(projectId);
        return gitOperations.getLatestCommit(localPath, branch);
    }

    /**
     * 当前简化实现：DiffParser 返回完整上下文，contextLines 参数暂不裁剪
     */
    public List<FileChange> getDiffWithContext(Long projectId, String sourceBranch, String targetBranch, int contextLines) {
        return getBranchDiff(projectId, sourceBranch, targetBranch);
    }

    private String resolveLocalPath(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException("PROJECT_NOT_FOUND", "项目不存在: " + projectId);
        }
        if (project.getLocalPath() == null) {
            throw new BusinessException("REPO_NOT_CLONED", "仓库尚未克隆");
        }
        return project.getLocalPath();
    }
}
