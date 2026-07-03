package com.review.agent.infrastructure.git;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析 GitLab 仓库 URL，提取 GitLab 实例地址和项目路径。
 * <p>
 * 支持的 URL 格式：
 * <ul>
 *   <li>HTTPS: {@code https://gitlab.com/group/project.git}</li>
 *   <li>HTTPS: {@code https://gitlab.example.com/group/subgroup/project}</li>
 *   <li>SSH:   {@code git@gitlab.com:group/project.git}</li>
 *   <li>SSH:   {@code git@gitlab.example.com:group/subgroup/project.git}</li>
 * </ul>
 */
public final class GitLabRepoUrlParser {

    private static final Pattern HTTPS_PATTERN =
            Pattern.compile("^https?://([^/]+)/(.+?)(?:\\.git)?$");

    private static final Pattern SSH_PATTERN =
            Pattern.compile("^git@([^:]+):(.+?)(?:\\.git)?$");

    private GitLabRepoUrlParser() {
    }

    /**
     * 解析 GitLab 仓库 URL。
     *
     * @param repoUrl 仓库地址（HTTPS 或 SSH 格式）
     * @return 解析结果，包含 host 和 projectPath；无法解析时返回 null
     */
    public static GitLabRepoInfo parse(String repoUrl) {
        if (repoUrl == null || repoUrl.isBlank()) {
            return null;
        }

        String host;
        String path;

        Matcher httpsMatcher = HTTPS_PATTERN.matcher(repoUrl.trim());
        if (httpsMatcher.matches()) {
            host = httpsMatcher.group(1);
            path = httpsMatcher.group(2);
        } else {
            Matcher sshMatcher = SSH_PATTERN.matcher(repoUrl.trim());
            if (sshMatcher.matches()) {
                host = sshMatcher.group(1);
                path = sshMatcher.group(2);
            } else {
                return null;
            }
        }

        // GitLab API 要求项目路径进行 URL 编码
        String encodedPath = URLEncoder.encode(path, StandardCharsets.UTF_8);
        return new GitLabRepoInfo(host, encodedPath);
    }

    @Getter
    @AllArgsConstructor
    public static class GitLabRepoInfo {
        /** GitLab 实例主机名，如 gitlab.com */
        private final String host;
        /** URL 编码后的项目路径，如 group%2Fproject */
        private final String projectPath;
    }
}
