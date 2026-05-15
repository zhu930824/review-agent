package com.review.agent.service.impl;

import com.review.agent.domain.dto.OperationDashboardVO;
import com.review.agent.domain.dto.OperationFindingVO;
import com.review.agent.domain.entity.Project;
import com.review.agent.domain.entity.Review;
import com.review.agent.domain.entity.ReviewFinding;
import com.review.agent.domain.enums.HumanStatus;
import com.review.agent.domain.enums.Severity;
import com.review.agent.infrastructure.persistence.ProjectMapper;
import com.review.agent.infrastructure.persistence.ReviewFindingMapper;
import com.review.agent.infrastructure.persistence.ReviewMapper;
import com.review.agent.service.OperationsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 运营面板服务实现 —— 批量加载关联数据避免 N+1 查询
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationsServiceImpl implements OperationsService {

    private final ReviewFindingMapper reviewFindingMapper;
    private final ReviewMapper reviewMapper;
    private final ProjectMapper projectMapper;

    @Override
    public OperationDashboardVO getDashboard() {
        // 1. 查询所有发现项
        List<ReviewFinding> allFindings = reviewFindingMapper.selectList(null);

        if (allFindings.isEmpty()) {
            return emptyDashboard();
        }

        // 2. 收集关联的 reviewId，批量加载 Review
        Map<Long, Review> reviewMap = batchLoadReviews(allFindings);

        // 3. 从 Review 中收集 projectId，批量加载 Project，构建 projectId -> name 映射
        Map<Long, String> projectNameMap = batchLoadProjectNames(reviewMap);

        // 4. 构建 OperationFindingVO 列表，附带项目名称
        List<OperationFindingVO> voList = allFindings.stream()
                .map(f -> toOperationFindingVO(f, reviewMap, projectNameMap))
                .collect(Collectors.toList());

        // 5. 计算统计指标（单次遍历完成所有计数）
        DashboardCounts counts = computeCounts(allFindings);

        OperationDashboardVO dashboard = new OperationDashboardVO();
        dashboard.setFindings(voList);
        dashboard.setTotalFindings(counts.total);
        dashboard.setBlockerCount(counts.blocker);
        dashboard.setMajorCount(counts.major);
        dashboard.setPendingCount(counts.pending);
        dashboard.setConfirmedCount(counts.confirmed);
        dashboard.setDismissedCount(counts.dismissed);
        // SLA 压力：BLOCKER 发现项占总数的百分比（BLOCKER 有 4h SLA 要求）
        dashboard.setSlaPressure(counts.total > 0
                ? (int) Math.round((double) counts.blocker / counts.total * 100)
                : 0);

        return dashboard;
    }

    /** 空面板：无发现项时返回全部为零的结果 */
    private OperationDashboardVO emptyDashboard() {
        OperationDashboardVO dashboard = new OperationDashboardVO();
        dashboard.setFindings(Collections.emptyList());
        // 其余 int 字段默认为 0，符合预期
        return dashboard;
    }

    /** 提取 reviewId，批量查询 Review，构建 id -> Review 映射 */
    private Map<Long, Review> batchLoadReviews(List<ReviewFinding> findings) {
        Set<Long> reviewIds = findings.stream()
                .map(ReviewFinding::getReviewId)
                .collect(Collectors.toSet());

        if (reviewIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return reviewMapper.selectBatchIds(reviewIds).stream()
                .collect(Collectors.toMap(Review::getId, Function.identity()));
    }

    /** 从 Review 映射中提取 projectId，批量查询 Project，构建 id -> name 映射 */
    private Map<Long, String> batchLoadProjectNames(Map<Long, Review> reviewMap) {
        Set<Long> projectIds = reviewMap.values().stream()
                .map(Review::getProjectId)
                .collect(Collectors.toSet());

        if (projectIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return projectMapper.selectBatchIds(projectIds).stream()
                .collect(Collectors.toMap(Project::getId, Project::getName));
    }

    /** 将 ReviewFinding 实体转为 OperationFindingVO，填充项目名称 */
    private OperationFindingVO toOperationFindingVO(
            ReviewFinding finding,
            Map<Long, Review> reviewMap,
            Map<Long, String> projectNameMap) {
        OperationFindingVO vo = new OperationFindingVO();
        vo.setId(finding.getId());
        vo.setReviewId(finding.getReviewId());
        vo.setSeverity(finding.getSeverity());
        vo.setCategory(finding.getCategory());
        vo.setTitle(finding.getTitle());
        vo.setHumanStatus(finding.getHumanStatus());
        vo.setConfidence(finding.getConfidence());
        vo.setIsCrossHit(finding.getIsCrossHit());

        // 通过 review -> project 链路获取项目名称
        Review review = reviewMap.get(finding.getReviewId());
        if (review != null) {
            vo.setProjectName(projectNameMap.get(review.getProjectId()));
        }

        return vo;
    }

    /** 单次遍历完成所有统计计数，避免多次 stream 遍历 */
    private DashboardCounts computeCounts(List<ReviewFinding> findings) {
        int total = findings.size();
        int blocker = 0;
        int major = 0;
        int pending = 0;
        int confirmed = 0;
        int dismissed = 0;

        for (ReviewFinding f : findings) {
            Severity sev = f.getSeverity();
            if (sev == Severity.BLOCKER) {
                blocker++;
            } else if (sev == Severity.MAJOR) {
                major++;
            }

            HumanStatus status = f.getHumanStatus();
            if (status == HumanStatus.PENDING) {
                pending++;
            } else if (status == HumanStatus.CONFIRMED) {
                confirmed++;
            } else if (status == HumanStatus.DISMISSED) {
                dismissed++;
            }
        }

        return new DashboardCounts(total, blocker, major, pending, confirmed, dismissed);
    }

    /** 内部计数聚合记录 */
    private static class DashboardCounts {
        final int total, blocker, major, pending, confirmed, dismissed;

        DashboardCounts(int total, int blocker, int major,
                        int pending, int confirmed, int dismissed) {
            this.total = total;
            this.blocker = blocker;
            this.major = major;
            this.pending = pending;
            this.confirmed = confirmed;
            this.dismissed = dismissed;
        }
    }
}
