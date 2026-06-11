package com.review.agent.infrastructure.persistence;

import com.review.agent.domain.dto.PrePrGateFindingInput;
import com.review.agent.domain.dto.PrePrGateVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PrePrGateSqlMapper {

    @Select("SELECT status FROM review WHERE id = #{reviewId}")
    Optional<String> findReviewStatus(@Param("reviewId") Long reviewId);

    @Select("""
            SELECT severity, human_status AS humanStatus, title
            FROM review_finding
            WHERE review_id = #{reviewId}
            """)
    List<PrePrGateFindingInput> listFindings(@Param("reviewId") Long reviewId);

    @Select("""
            SELECT id, review_id, gate_status, summary, blocked_reasons, decided_by, decided_at, created_at, updated_at
            FROM pre_pr_gate
            WHERE review_id = #{reviewId}
            """)
    @Results(id = "prePrGateResult", value = {
            @Result(column = "review_id", property = "reviewId"),
            @Result(column = "gate_status", property = "gateStatus"),
            @Result(column = "blocked_reasons", property = "blockedReasonsJson"),
            @Result(column = "decided_by", property = "decidedBy"),
            @Result(column = "decided_at", property = "decidedAt"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    Optional<PrePrGateRow> findGate(@Param("reviewId") Long reviewId);

    @Insert("""
            INSERT INTO pre_pr_gate
                (review_id, gate_status, summary, blocked_reasons, decided_by, decided_at, created_at, updated_at)
            VALUES
                (#{reviewId}, #{gateStatus}, #{summary}, #{blockedReasonsJson}, #{decidedBy}, #{decidedAt}, #{createdAt}, #{updatedAt})
            ON DUPLICATE KEY UPDATE
                gate_status = VALUES(gate_status),
                summary = VALUES(summary),
                blocked_reasons = VALUES(blocked_reasons),
                decided_by = VALUES(decided_by),
                decided_at = VALUES(decided_at),
                updated_at = VALUES(updated_at)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int upsertGate(PrePrGateRow row);

    @Insert("""
            INSERT INTO pre_pr_gate_history
                (review_id, gate_status, event_type, reason, operator, created_at)
            VALUES
                (#{reviewId}, #{gateStatus}, #{eventType}, #{reason}, #{operator}, CURRENT_TIMESTAMP)
            """)
    int appendGateHistory(
            @Param("reviewId") Long reviewId,
            @Param("gateStatus") String gateStatus,
            @Param("eventType") String eventType,
            @Param("reason") String reason,
            @Param("operator") String operator);
}
