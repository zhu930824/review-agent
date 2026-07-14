package com.review.agent.infrastructure.git;

import com.review.agent.domain.entity.ProjectGitLabConfig;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class GitLabMergeRequestNoteClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public String post(ProjectGitLabConfig config, String mergeRequestIid, String body) {
        String requestUrl = requestUrl(config, mergeRequestIid);
        HttpHeaders headers = new HttpHeaders();
        headers.set("PRIVATE-TOKEN", config.getGitlabToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.postForEntity(
                requestUrl,
                new HttpEntity<>(Map.of("body", body), headers),
                String.class);
        return requestUrl;
    }

    public String requestUrl(ProjectGitLabConfig config, String mergeRequestIid) {
        String host = config.getGitlabHost().startsWith("http")
                ? config.getGitlabHost()
                : "https://" + config.getGitlabHost();
        return host + "/api/v4/projects/" + config.getProjectPath()
                + "/merge_requests/" + mergeRequestIid + "/notes";
    }
}
