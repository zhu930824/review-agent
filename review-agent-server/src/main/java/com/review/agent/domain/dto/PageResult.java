package com.review.agent.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 通用分页响应
 */
@Data
@AllArgsConstructor
public class PageResult<T> {

    private long total;
    private int pageNum;
    private int pageSize;
    private List<T> records;
}
