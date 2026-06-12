package com.review.agent.infrastructure.ci;

import com.review.agent.domain.entity.CiStatusConfig;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GitHubSarifUploadRequestFactoryTest {

    private final GitHubSarifUploadRequestFactory factory = new GitHubSarifUploadRequestFactory();

    @Test
    void buildsGitHubCodeScanningSarifUploadRequest() throws Exception {
        String sarif = "{\"version\":\"2.1.0\",\"runs\":[]}";

        GitHubSarifUploadRequest request = factory.build(config(), "abc123", "refs/heads/main", sarif);

        assertEquals("https://api.github.com/repos/zhu930824/review-agent/code-scanning/sarifs", request.url());
        assertEquals("Bearer ghp_secret", request.headers().get("Authorization"));
        assertEquals("application/vnd.github+json", request.headers().get("Accept"));
        assertEquals("2026-03-10", request.headers().get("X-GitHub-Api-Version"));
        assertEquals("abc123", request.body().get("commit_sha"));
        assertEquals("refs/heads/main", request.body().get("ref"));
        assertEquals("Review Agent", request.body().get("tool_name"));
        assertEquals(true, request.body().get("validate"));
        assertEquals(sarif, gunzipBase64((String) request.body().get("sarif")));
    }

    private String gunzipBase64(String encoded) throws Exception {
        byte[] compressed = Base64.getDecoder().decode(encoded);
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
            return new String(gzip.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private CiStatusConfig config() {
        CiStatusConfig config = new CiStatusConfig();
        config.setProvider("GITHUB");
        config.setRepoOwner("zhu930824");
        config.setRepoName("review-agent");
        config.setApiToken("ghp_secret");
        return config;
    }
}
