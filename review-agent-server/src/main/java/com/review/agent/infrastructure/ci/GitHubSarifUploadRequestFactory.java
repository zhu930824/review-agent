package com.review.agent.infrastructure.ci;

import com.review.agent.domain.entity.CiStatusConfig;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

@Component
public class GitHubSarifUploadRequestFactory {

    public GitHubSarifUploadRequest build(CiStatusConfig config, String commitSha, String ref, String sarif) {
        String url = "https://api.github.com/repos/%s/%s/code-scanning/sarifs"
                .formatted(config.getRepoOwner(), config.getRepoName());

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Authorization", "Bearer " + config.getApiToken());
        headers.put("Accept", "application/vnd.github+json");
        headers.put("X-GitHub-Api-Version", "2026-03-10");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("commit_sha", commitSha);
        body.put("ref", ref);
        body.put("sarif", gzipBase64(sarif));
        body.put("tool_name", "Review Agent");
        body.put("validate", true);

        return new GitHubSarifUploadRequest(url, headers, body);
    }

    private String gzipBase64(String sarif) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
                gzip.write((sarif == null ? "" : sarif).getBytes(StandardCharsets.UTF_8));
            }
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to compress SARIF payload", ex);
        }
    }
}
