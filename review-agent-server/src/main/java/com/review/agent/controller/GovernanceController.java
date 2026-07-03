package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.dto.GovernanceCapabilityVO;
import com.review.agent.domain.dto.GovernanceRulePackDryRunVO;
import com.review.agent.domain.dto.GovernanceRulePackChangeVO;
import com.review.agent.domain.dto.GovernanceRulePackVersionVO;
import com.review.agent.domain.dto.GovernanceRulePackVO;
import com.review.agent.domain.dto.IntegrationConnectorVO;
import com.review.agent.domain.dto.WorkflowTemplateVO;
import com.review.agent.infrastructure.persistence.GovernanceRulePackChangeRepository;
import com.review.agent.infrastructure.persistence.GovernanceRulePackVersionRepository;
import com.review.agent.service.GovernanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/governance")
@RequiredArgsConstructor
public class GovernanceController {

    private final GovernanceService governanceService;
    private final GovernanceRulePackChangeRepository rulePackChangeRepository;
    private final GovernanceRulePackVersionRepository rulePackVersionRepository;

    @GetMapping("/capabilities")
    public Result<List<GovernanceCapabilityVO>> listCapabilities() {
        return Result.success(governanceService.listCapabilities());
    }

    @GetMapping("/connectors")
    public Result<List<IntegrationConnectorVO>> listConnectors() {
        return Result.success(governanceService.listConnectors());
    }

    @GetMapping("/rule-packs")
    public Result<List<GovernanceRulePackVO>> listRulePacks() {
        return Result.success(governanceService.listRulePacks());
    }

    @GetMapping("/workflows")
    public Result<List<WorkflowTemplateVO>> listWorkflows() {
        return Result.success(governanceService.listWorkflows());
    }

    @GetMapping("/rule-pack-changes")
    public Result<List<GovernanceRulePackChangeVO>> listRulePackChanges(@RequestParam(defaultValue = "20") int limit) {
        return Result.success(rulePackChangeRepository.listRecent(limit));
    }

    @GetMapping("/rule-pack-versions")
    public Result<List<GovernanceRulePackVersionVO>> listRulePackVersions(@RequestParam(defaultValue = "20") int limit) {
        return Result.success(rulePackVersionRepository.listRecent(limit));
    }

    @PostMapping("/rule-pack-changes/{id}/approve")
    public Result<Void> approveRulePackChange(@PathVariable("id") Long id) {
        rulePackChangeRepository.updateStatus(id, "APPROVED");
        return Result.success();
    }

    @PostMapping("/rule-pack-changes/{id}/dry-run")
    public Result<GovernanceRulePackDryRunVO> dryRunRulePackChange(@PathVariable("id") Long id) {
        return Result.success(rulePackVersionRepository.dryRunChange(id));
    }

    @PostMapping("/rule-pack-changes/{id}/apply")
    public Result<Void> applyRulePackChange(@PathVariable("id") Long id) {
        rulePackVersionRepository.applyChange(id);
        rulePackChangeRepository.updateStatus(id, "APPLIED");
        return Result.success();
    }

    @PostMapping("/rule-pack-changes/{id}/reject")
    public Result<Void> rejectRulePackChange(@PathVariable("id") Long id) {
        rulePackChangeRepository.updateStatus(id, "REJECTED");
        return Result.success();
    }

    @PostMapping("/rule-pack-changes/{id}/rollback")
    public Result<Void> rollbackRulePackChange(@PathVariable("id") Long id) {
        rulePackVersionRepository.rollbackChange(id);
        rulePackChangeRepository.updateStatus(id, "ROLLED_BACK");
        return Result.success();
    }
}
