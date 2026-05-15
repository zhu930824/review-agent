package com.review.agent.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.review.agent.domain.entity.WorkflowTemplate;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WorkflowTemplateMapper extends BaseMapper<WorkflowTemplate> {
}
