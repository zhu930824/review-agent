package com.review.agent.infrastructure.git;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.review.agent.common.exception.BizException;
import com.review.agent.domain.dto.diff.FileChange;
import com.review.agent.domain.entity.ProjectGitLabConfig;
import com.review.agent.domain.exception.CommonExceptionEnum;
import com.review.agent.infrastructure.persistence.ProjectGitLabConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * GitLab API 模式下的 diff 服务。
 * 通过 GitLab REST API 直接获取分支/提交之间的 diff，
 * 无需在本地克隆仓库。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GitLabDiffService {

    private final GitLabApiClient gitLabApiClient;
    private final DiffParser diffParser;
    private final ProjectGitLabConfigMapper gitLabConfigMapper;

    /**
     * 通过 GitLab API 获取两个分支之间的 diff。
     *
     * @param projectId    项目 ID
     * @param sourceBranch 源分支
     * @param targetBranch 目标分支
     * @return 解析后的文件变更列表
     */
    public List<FileChange> getBranchDiff(Long projectId, String sourceBranch, String targetBranch) {
        ProjectGitLabConfig config = requireConfig(projectId);
        GitLabCompareResponse compareResp = gitLabApiClient.compare(config, sourceBranch, targetBranch);

        if (compareResp == null || compareResp.getDiffs() == null || compareResp.getDiffs().isEmpty()) {
            log.info("[GitLab Diff] 无差异: projectId={}, {}..{}", projectId, targetBranch, sourceBranch);
            return List.of();
        }

        // 将 GitLab API 返回的多个文件 diff 拼接成完整的 unified diff
        // 注意：GitLab API 每个 diff 中的 diff 字段均以 "--- a/..." 开头，
        //       但缺少 "diff --git a/... b/..." 前导行，因此需补齐，以使 DiffParser 正确解析。
        String fullDiff = compareResp.getDiffs().stream()
                .map(d -> "diff --git a/" + d.getOldPath() + " b/" + d.getNewPath() + "\n" + d.getDiff())
                .collect(Collectors.joining());

        return diffParser.parse(fullDiff);
    }

    /**
     * 通过 GitLab API 获取两个 commit 之间的 diff。
     */
    public List<FileChange> getCommitDiff(Long projectId, String sourceCommit, String targetCommit) {
        ProjectGitLabConfig config = requireConfig(projectId);
        GitLabCompareResponse compareResp = gitLabApiClient.compare(config, sourceCommit, targetCommit);

        if (compareResp == null || compareResp.getDiffs() == null || compareResp.getDiffs().isEmpty()) {
            log.info("[GitLab Diff] 无差异: projectId={}, {}..{}", projectId, targetCommit, sourceCommit);
            return List.of();
        }

        String fullDiff = compareResp.getDiffs().stream()
                .map(d -> "diff --git a/" + d.getOldPath() + " b/" + d.getNewPath() + "\n" + d.getDiff())
                .collect(Collectors.joining());

        return diffParser.parse(fullDiff);
    }

    /**
     * 通过 GitLab API 获取指定分支的最新 commit SHA。
     */
    public String getLatestCommit(Long projectId, String branch) {
        ProjectGitLabConfig config = requireConfig(projectId);
        return gitLabApiClient.getLatestCommit(config, branch);
    }

    /**
     * 通过 GitLab API 获取仓库分支列表。
     */
    public List<String> getBranches(Long projectId) {
        ProjectGitLabConfig config = requireConfig(projectId);
        return gitLabApiClient.getBranches(config);
    }

    /**
     * 检查项目是否配置了 GitLab API 模式。
     */
    public boolean isGitLabConfigured(Long projectId) {
        ProjectGitLabConfig config = findByProjectId(projectId);
        return config != null && Boolean.TRUE.equals(config.getEnabled());
    }

    private ProjectGitLabConfig requireConfig(Long projectId) {
        ProjectGitLabConfig config = findByProjectId(projectId);
        if (config == null) {
            throw new BizException(CommonExceptionEnum.GIT_FETCH_FAILED);
        }
        return config;
    }

    private ProjectGitLabConfig findByProjectId(Long projectId) {
        LambdaQueryWrapper<ProjectGitLabConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectGitLabConfig::getProjectId, projectId);
        return gitLabConfigMapper.selectOne(wrapper);
    }
}
