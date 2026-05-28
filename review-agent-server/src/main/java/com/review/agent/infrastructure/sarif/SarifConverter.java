package com.review.agent.infrastructure.sarif;

import com.review.agent.domain.entity.Review;
import com.review.agent.domain.entity.ReviewFinding;
import com.review.agent.domain.enums.Severity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class SarifConverter {

    private static final String SARIF_SCHEMA =
            "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json";
    private static final String SARIF_VERSION = "2.1.0";
    private static final String TOOL_NAME = "Review Agent";
    private static final String TOOL_VERSION = "1.0.0";
    private static final String TOOL_ORG = "AI Engineering Governance Platform";

    public SarifLog convert(Review review, List<ReviewFinding> findings) {
        AtomicInteger ruleIndex = new AtomicInteger(0);
        Map<String, Integer> ruleIndexMap = new TreeMap<>();

        List<SarifLog.Result> results = new ArrayList<>();
        for (ReviewFinding f : findings) {
            String ruleId = buildRuleId(f);
            int idx = ruleIndexMap.computeIfAbsent(ruleId, k -> ruleIndex.getAndIncrement());

            results.add(SarifLog.Result.builder()
                    .ruleId(ruleId)
                    .ruleIndex(idx)
                    .level(mapLevel(f.getSeverity()))
                    .message(SarifLog.Message.builder()
                            .text(f.getTitle())
                            .markdown(buildMarkdown(f))
                            .build())
                    .locations(buildLocations(f))
                    .kind("fail")
                    .build());
        }

        return SarifLog.builder()
                .version(SARIF_VERSION)
                .schema(SARIF_SCHEMA)
                .runs(List.of(SarifLog.Run.builder()
                        .tool(SarifLog.Tool.builder()
                                .driver(SarifLog.Driver.builder()
                                        .name(TOOL_NAME)
                                        .version(TOOL_VERSION)
                                        .organization(TOOL_ORG)
                                        .informationUri("https://review-agent.local")
                                        .build())
                                .build())
                        .columnKind("utf16CodeUnits")
                        .results(results)
                        .build()))
                .build();
    }

    private String buildRuleId(ReviewFinding f) {
        return String.format("RA-%s-%s",
                f.getCategory() != null ? f.getCategory().name().replace("_", "-") : "OTHER",
                f.getSeverity() != null ? f.getSeverity().name() : "INFO");
    }

    private String mapLevel(Severity severity) {
        if (severity == null) {
            return "none";
        }
        return switch (severity) {
            case BLOCKER -> "error";
            case MAJOR -> "error";
            case MINOR -> "warning";
            case INFO -> "note";
        };
    }

    private String buildMarkdown(ReviewFinding f) {
        StringBuilder sb = new StringBuilder();
        sb.append("**").append(f.getTitle()).append("**\n\n");
        if (f.getDescription() != null) {
            sb.append(f.getDescription()).append("\n\n");
        }
        if (f.getSuggestion() != null) {
            sb.append("**建议改动:** ").append(f.getSuggestion()).append("\n\n");
        }
        if (f.getModelName() != null) {
            sb.append("*来源模型: ").append(f.getModelName()).append("*");
        }
        return sb.toString();
    }

    private List<SarifLog.Location> buildLocations(ReviewFinding f) {
        SarifLog.Region region = SarifLog.Region.builder()
                .startLine(f.getLineStart())
                .endLine(f.getLineEnd() != null ? f.getLineEnd() : f.getLineStart())
                .startColumn(1)
                .endColumn(1)
                .build();

        return List.of(SarifLog.Location.builder()
                .physicalLocation(SarifLog.PhysicalLocation.builder()
                        .artifactLocation(SarifLog.ArtifactLocation.builder()
                                .uri(f.getFilePath())
                                .build())
                        .region(region)
                        .build())
                .build());
    }
}
