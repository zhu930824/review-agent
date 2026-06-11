package com.review.agent.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.review.agent.domain.dto.PrePrGateFindingInput;
import com.review.agent.domain.dto.PrePrGateVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MybatisPrePrGateRepository implements PrePrGateRepository {

    private final PrePrGateSqlMapper mapper;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<String> findReviewStatus(Long reviewId) {
        return mapper.findReviewStatus(reviewId);
    }

    @Override
    public List<PrePrGateFindingInput> listFindings(Long reviewId) {
        return mapper.listFindings(reviewId);
    }

    @Override
    public Optional<PrePrGateVO> findGate(Long reviewId) {
        return mapper.findGate(reviewId).map(this::toVO);
    }

    @Override
    public PrePrGateVO upsertGate(PrePrGateVO gate) {
        mapper.upsertGate(toRow(gate));
        return findGate(gate.getReviewId()).orElse(gate);
    }

    @Override
    public void appendGateHistory(Long reviewId, String gateStatus, String eventType, String reason, String operator) {
        mapper.appendGateHistory(reviewId, gateStatus, eventType, reason, operator);
    }

    private PrePrGateVO toVO(PrePrGateRow row) {
        PrePrGateVO vo = new PrePrGateVO();
        vo.setId(row.getId());
        vo.setReviewId(row.getReviewId());
        vo.setGateStatus(row.getGateStatus());
        vo.setSummary(row.getSummary());
        vo.setBlockedReasons(parseReasons(row.getBlockedReasonsJson()));
        vo.setDecidedBy(row.getDecidedBy());
        vo.setDecidedAt(row.getDecidedAt());
        vo.setCreatedAt(row.getCreatedAt());
        vo.setUpdatedAt(row.getUpdatedAt());
        return vo;
    }

    private PrePrGateRow toRow(PrePrGateVO vo) {
        PrePrGateRow row = new PrePrGateRow();
        row.setId(vo.getId());
        row.setReviewId(vo.getReviewId());
        row.setGateStatus(vo.getGateStatus());
        row.setSummary(vo.getSummary());
        row.setBlockedReasonsJson(writeReasons(vo.getBlockedReasons()));
        row.setDecidedBy(vo.getDecidedBy());
        row.setDecidedAt(vo.getDecidedAt());
        row.setCreatedAt(vo.getCreatedAt());
        row.setUpdatedAt(vo.getUpdatedAt());
        return row;
    }

    private List<String> parseReasons(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            return List.of(json);
        }
    }

    private String writeReasons(List<String> reasons) {
        try {
            return objectMapper.writeValueAsString(reasons == null ? List.of() : reasons);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
