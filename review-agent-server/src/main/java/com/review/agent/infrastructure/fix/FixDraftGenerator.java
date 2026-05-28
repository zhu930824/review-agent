package com.review.agent.infrastructure.fix;

import com.review.agent.domain.entity.ReviewFinding;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FixDraftGenerator {

    public List<FixDraft> generate(List<ReviewFinding> findings) {
        List<FixDraft> drafts = new ArrayList<>();
        if (findings == null) return drafts;

        for (ReviewFinding f : findings) {
            FixDraft draft = generateForFinding(f);
            if (draft != null) {
                drafts.add(draft);
            }
        }
        return drafts;
    }

    private FixDraft generateForFinding(ReviewFinding f) {
        if (f.getSuggestion() == null || f.getSuggestion().isBlank()) {
            return null;
        }

        return FixDraft.builder()
                .filePath(f.getFilePath())
                .lineStart(f.getLineStart())
                .lineEnd(f.getLineEnd())
                .originalContent(f.getTitle())
                .fixedContent(f.getSuggestion())
                .explanation("根据规则引擎建议生成的修复草案")
                .confidence("MEDIUM")
                .needsHumanReview(true)
                .build();
    }
}
