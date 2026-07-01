package com.review.agent.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.review.agent.domain.entity.ModelCallTelemetry;
import com.review.agent.domain.entity.ReviewFinding;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MybatisModelTelemetryRepository implements ModelTelemetryRepository {

    private final ModelTelemetryMapper modelTelemetryMapper;
    private final ReviewFindingMapper reviewFindingMapper;

    @Override
    public ModelCallTelemetry insert(ModelCallTelemetry telemetry) {
        modelTelemetryMapper.insert(telemetry);
        return telemetry;
    }

    @Override
    public List<ModelCallTelemetry> listRecent(int limit) {
        return modelTelemetryMapper.selectList(
                new LambdaQueryWrapper<ModelCallTelemetry>()
                        .orderByDesc(ModelCallTelemetry::getCreatedAt)
                        .last("LIMIT " + limit));
    }

    @Override
    public List<ReviewFinding> listFindingsByReviewIds(List<Long> reviewIds) {
        if (reviewIds == null || reviewIds.isEmpty()) {
            return Collections.emptyList();
        }
        return reviewFindingMapper.selectList(
                new LambdaQueryWrapper<ReviewFinding>()
                        .in(ReviewFinding::getReviewId, reviewIds));
    }
}
