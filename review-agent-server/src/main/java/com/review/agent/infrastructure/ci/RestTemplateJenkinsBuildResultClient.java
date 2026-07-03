package com.review.agent.infrastructure.ci;

import com.review.agent.domain.entity.CiStatusConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RestTemplateJenkinsBuildResultClient implements JenkinsBuildResultClient {

    private final JenkinsGateRequestFactory requestFactory;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public JenkinsQueueSnapshot fetchQueue(CiStatusConfig config, String queueUrl) {
        Map<?, ?> body = get(config, appendApiJson(queueUrl));
        if (body == null) {
            return new JenkinsQueueSnapshot(queueUrl, null, null, "QUEUED");
        }
        Object cancelled = body.get("cancelled");
        if (Boolean.TRUE.equals(cancelled)) {
            return new JenkinsQueueSnapshot(queueUrl, null, null, "CANCELLED");
        }
        Object executable = body.get("executable");
        if (executable instanceof Map<?, ?> executableMap) {
            String buildUrl = value(executableMap.get("url"));
            String buildNumber = value(executableMap.get("number"));
            return new JenkinsQueueSnapshot(queueUrl, buildUrl, buildNumber, "BUILDING");
        }
        return new JenkinsQueueSnapshot(queueUrl, null, null, "QUEUED");
    }

    @Override
    public JenkinsBuildSnapshot fetchBuild(CiStatusConfig config, String buildUrl) {
        Map<?, ?> body = get(config, appendApiJson(buildUrl));
        if (body == null) {
            return new JenkinsBuildSnapshot(buildUrl, null, "BUILDING", true);
        }
        boolean building = Boolean.TRUE.equals(body.get("building"));
        String result = value(body.get("result"));
        String number = value(body.get("number"));
        return new JenkinsBuildSnapshot(
                value(body.get("url"), buildUrl),
                number,
                building ? "BUILDING" : value(result, "UNKNOWN"),
                building);
    }

    private Map<?, ?> get(CiStatusConfig config, String url) {
        HttpHeaders headers = new HttpHeaders();
        requestFactory.buildCrumbHeaders(config).forEach(headers::set);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        return response.getBody();
    }

    private String appendApiJson(String url) {
        String normalized = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        return normalized + "/api/json";
    }

    private String value(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String value(Object value, String fallback) {
        String resolved = value(value);
        return resolved == null || resolved.isBlank() ? fallback : resolved;
    }
}
