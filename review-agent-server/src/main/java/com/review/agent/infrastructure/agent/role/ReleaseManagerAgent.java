package com.review.agent.infrastructure.agent.role;

import com.review.agent.infrastructure.workflow.ReleaseWorkflowService;
import com.review.agent.infrastructure.workflow.WorkflowStep;
import com.review.agent.infrastructure.sop.SopDocument;
import com.review.agent.infrastructure.sop.SopSection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReleaseManagerAgent {

    private final ReleaseWorkflowService releaseWorkflowService;

    public SopDocument generateReleaseChecklist() {
        List<WorkflowStep> steps = releaseWorkflowService.getSteps();

        List<SopSection> sections = steps.stream()
                .map(s -> SopSection.builder()
                        .title(s.getName())
                        .category("RELEASE")
                        .description(s.getType() + " - " + s.getStatus())
                        .defaultSeverity(s.getStatus().equals("REQUIRED") ? "BLOCKER" : "MAJOR")
                        .actionable(true)
                        .violationMessage("跳过 " + s.getName())
                        .fixSuggestion("请完成 " + s.getName() + " 后再继续发布")
                        .build())
                .toList();

        return SopDocument.builder()
                .title("AI 发布检查清单")
                .version("1.0.0")
                .team("Engineering")
                .sections(sections)
                .build();
    }

    public String executePreFlight(Long reviewId) {
        String workflowId = releaseWorkflowService.startReleaseWorkflow(reviewId);
        if (workflowId == null) {
            log.warn("Pre-PR 门禁未通过，发布预检不通过: reviewId={}", reviewId);
            return null;
        }
        log.info("发布预检通过: reviewId={}, workflowId={}", reviewId, workflowId);
        return workflowId;
    }
}
