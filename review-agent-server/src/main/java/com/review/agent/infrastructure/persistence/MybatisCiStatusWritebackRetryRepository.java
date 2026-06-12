package com.review.agent.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.review.agent.domain.entity.CiStatusWritebackLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MybatisCiStatusWritebackRetryRepository implements CiStatusWritebackRetryRepository {

    private final CiStatusWritebackLogMapper mapper;

    @Override
    public Optional<CiStatusWritebackLog> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    @Override
    public List<CiStatusWritebackLog> findDueFailedWritebacks(LocalDateTime now, int limit) {
        return mapper.selectList(new LambdaQueryWrapper<CiStatusWritebackLog>()
                .eq(CiStatusWritebackLog::getWritebackStatus, "FAILED")
                .isNotNull(CiStatusWritebackLog::getNextRetryAt)
                .le(CiStatusWritebackLog::getNextRetryAt, now)
                .orderByAsc(CiStatusWritebackLog::getNextRetryAt)
                .last("LIMIT " + Math.max(1, limit)));
    }

    @Override
    public void markRetrying(CiStatusWritebackLog log) {
        mapper.updateById(log);
    }
}
