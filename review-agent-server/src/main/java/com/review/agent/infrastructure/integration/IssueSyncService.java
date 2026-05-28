package com.review.agent.infrastructure.integration;

import com.review.agent.domain.entity.ReviewFinding;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class IssueSyncService {

    private final Map<String, IssueTracker> trackers;

    public IssueSyncService(List<IssueTracker> trackerList) {
        trackers = trackerList.stream()
                .collect(Collectors.toMap(IssueTracker::source, Function.identity()));
    }

    public String sync(String source, ReviewFinding finding) {
        IssueTracker tracker = trackers.get(source);
        if (tracker == null) {
            log.warn("未找到 IssueTracker: source={}", source);
            return null;
        }
        String issueId = tracker.createIssue(
                "[Review] " + finding.getTitle(),
                (finding.getDescription() != null ? finding.getDescription() + "\n" : "")
                        + "文件: " + finding.getFilePath() + "\n建议: " + finding.getSuggestion(),
                finding.getSeverity() != null ? finding.getSeverity().name() : "INFO",
                null
        );
        log.info("同步工单: finding={}, source={}, issueId={}", finding.getId(), source, issueId);
        return issueId;
    }

    public List<String> listSources() {
        return List.copyOf(trackers.keySet());
    }
}
