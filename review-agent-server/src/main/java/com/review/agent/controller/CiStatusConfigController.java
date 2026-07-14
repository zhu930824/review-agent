package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.dto.CiIntegrationHealthVO;
import com.review.agent.domain.dto.CiStatusConfigVO;
import com.review.agent.domain.dto.CiStatusWritebackLogVO;
import com.review.agent.domain.dto.UpsertCiStatusConfigRequest;
import com.review.agent.domain.dto.CiConnectionTestResultVO;
import com.review.agent.domain.dto.CredentialSecurityHealthVO;
import com.review.agent.domain.dto.CredentialRotationRequest;
import com.review.agent.domain.dto.CredentialRotationResultVO;
import com.review.agent.service.CiStatusConfigService;
import com.review.agent.service.CiStatusWritebackLogService;
import com.review.agent.service.CiStatusWritebackRetryService;
import com.review.agent.service.JenkinsBuildResultRefreshService;
import com.review.agent.service.CiConnectionTestService;
import com.review.agent.service.CredentialSecurityHealthService;
import com.review.agent.service.CredentialRotationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/integration/ci-config")
@RequiredArgsConstructor
public class CiStatusConfigController {

    private final CiStatusConfigService ciStatusConfigService;
    private final CiStatusWritebackLogService ciStatusWritebackLogService;
    private final CiStatusWritebackRetryService ciStatusWritebackRetryService;
    private final JenkinsBuildResultRefreshService jenkinsBuildResultRefreshService;
    private final CiConnectionTestService ciConnectionTestService;
    private final CredentialSecurityHealthService credentialSecurityHealthService;
    private final CredentialRotationService credentialRotationService;

    @GetMapping
    public Result<CiStatusConfigVO> getConfig(
            @RequestParam(value = "connectorKey", defaultValue = "github-checks") String connectorKey) {
        return Result.success(ciStatusConfigService.getConfig(connectorKey));
    }

    @GetMapping("/list")
    public Result<List<CiStatusConfigVO>> listConfigs(
            @RequestParam(value = "provider", required = false) String provider) {
        return Result.success(ciStatusConfigService.listConfigs(provider));
    }

    @PutMapping
    public Result<CiStatusConfigVO> upsertConfig(@Valid @RequestBody UpsertCiStatusConfigRequest request) {
        return Result.success(ciStatusConfigService.upsertConfig(request));
    }

    @DeleteMapping
    public Result<Void> deleteConfig(@RequestParam("connectorKey") String connectorKey) {
        ciStatusConfigService.deleteConfig(connectorKey);
        return Result.success();
    }

    @PostMapping("/test")
    public Result<CiConnectionTestResultVO> testConnection(
            @RequestParam("connectorKey") String connectorKey) {
        return Result.success(ciConnectionTestService.test(connectorKey));
    }

    @GetMapping("/credential-health")
    public Result<CredentialSecurityHealthVO> getCredentialSecurityHealth() {
        return Result.success(credentialSecurityHealthService.getHealth());
    }

    @PostMapping("/credential-rotation")
    public Result<CredentialRotationResultVO> rotateCredentials(
            @Valid @RequestBody CredentialRotationRequest request) {
        return Result.success(credentialRotationService.rotate(request));
    }

    @GetMapping("/writebacks")
    public Result<List<CiStatusWritebackLogVO>> listWritebacks(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return Result.success(ciStatusWritebackLogService.listRecent(limit));
    }

    @GetMapping("/writebacks/health")
    public Result<List<CiIntegrationHealthVO>> listWritebackHealth(
            @RequestParam(value = "limit", defaultValue = "50") int limit) {
        return Result.success(ciStatusWritebackLogService.listHealth(limit));
    }

    @PostMapping("/writebacks/{id}/retry")
    public Result<Void> retryWriteback(@PathVariable("id") Long writebackLogId) {
        ciStatusWritebackRetryService.retry(writebackLogId);
        return Result.success();
    }

    @PostMapping("/writebacks/jenkins/refresh")
    public Result<Integer> refreshJenkinsWritebackResults(
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        return Result.success(jenkinsBuildResultRefreshService.refreshRecent(limit));
    }
}
