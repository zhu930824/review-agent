package com.review.agent.infrastructure.git;

import com.review.agent.common.exception.BizException;
import com.review.agent.domain.entity.ProjectGitLabConfig;
import com.review.agent.domain.exception.CommonExceptionEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * GitLab REST API 客户端。
 * 通过 Personal Access Token 认证，调用 GitLab API 获取仓库信息。
 */
@Slf4j
@Component
public class GitLabApiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 调用 GitLab Compare API 获取两个引用之间的 diff。
     *
     * @param config GitLab 集成配置
     * @param from   源引用（分支名或 commit SHA）
     * @param to     目标引用（分支名或 commit SHA）
     * @return GitLab API 响应
     */
    public GitLabCompareResponse compare(ProjectGitLabConfig config, String from, String to) {
        String url = buildApiUrl(config, "/projects/%s/repository/compare"
                .formatted(config.getProjectPath()));
        url += "?from=" + encode(from) + "&to=" + encode(to);

        log.info("[GitLab API] compare: from={}, to={}, projectPath={}", from, to, config.getProjectPath());
        return get(url, config.getGitlabToken(), GitLabCompareResponse.class);
    }

    /**
     * 获取指定分支的最新 commit SHA。
     *
     * @param config GitLab 集成配置
     * @param branch 分支名
     * @return commit SHA
     */
    public String getLatestCommit(ProjectGitLabConfig config, String branch) {
        String url = buildApiUrl(config, "/projects/%s/repository/commits"
                .formatted(config.getProjectPath()));
        url += "?ref_name=" + encode(branch) + "&per_page=1";

        log.info("[GitLab API] getLatestCommit: branch={}", branch);
        List<Map<String, Object>> commits = getList(url, config.getGitlabToken());
        if (commits == null || commits.isEmpty()) {
            throw new BizException(CommonExceptionEnum.GIT_RESOLVE_FAILED);
        }
        return (String) commits.get(0).get("id");
    }

    /**
     * 获取仓库分支列表。
     *
     * @param config GitLab 集成配置
     * @return 分支名称列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getBranches(ProjectGitLabConfig config) {
        String url = buildApiUrl(config, "/projects/%s/repository/branches"
                .formatted(config.getProjectPath()));
        url += "?per_page=100";

        log.info("[GitLab API] getBranches: projectPath={}", config.getProjectPath());
        List<Map<String, Object>> branches = getList(url, config.getGitlabToken());
        if (branches == null) {
            return List.of();
        }
        return branches.stream()
                .map(b -> (String) b.get("name"))
                .toList();
    }

    private <T> T get(String url, String token, Class<T> responseType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("PRIVATE-TOKEN", token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return null;
        } catch (RestClientException e) {
            log.error("[GitLab API] 请求失败: url={}, error={}", url, e.getMessage());
            throw new BizException(CommonExceptionEnum.GIT_FETCH_FAILED, e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getList(String url, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("PRIVATE-TOKEN", token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (List<Map<String, Object>>) response.getBody();
            }
            return List.of();
        } catch (RestClientException e) {
            log.error("[GitLab API] 请求失败: url={}, error={}", url, e.getMessage());
            throw new BizException(CommonExceptionEnum.GIT_FETCH_FAILED, e);
        }
    }

    private String buildApiUrl(ProjectGitLabConfig config, String path) {
        String host = config.getGitlabHost();
        if (!host.startsWith("http")) {
            host = "https://" + host;
        }
        if (!host.endsWith("/")) {
            host += "/";
        }
        return host + "api/v4" + path;
    }

    private String encode(String value) {
        try {
            return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }
}
