package com.review.agent.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.review.agent.domain.entity.ProjectGitLabConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * GitLab 集成配置 Mapper
 */
@Mapper
public interface ProjectGitLabConfigMapper extends BaseMapper<ProjectGitLabConfig> {
}
