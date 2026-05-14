package com.review.agent.infrastructure.agent.skill;

import org.springframework.stereotype.Component;

@Component
public class PerformanceSkill implements Skill {

    @Override
    public String getName() { return "PerformanceSkill"; }

    @Override
    public String getDescription() { return "代码性能检测规则"; }

    @Override
    public String getKnowledgeRules() {
        return """
            你是代码性能分析专家。请按以下规则审查：

            【数据库操作】
            - 严禁在循环内执行数据库查询（N+1 问题）
            - 批量操作使用 batchInsert/batchUpdate 而非逐条操作
            - 查询字段避免使用 SELECT *，只查询需要的列
            - 大数据量查询必须分页，添加合理的 LIMIT

            【资源管理】
            - 数据库连接、文件流、网络连接必须在 finally 或 try-with-resources 中关闭
            - 使用连接池管理数据库连接，避免频繁创建销毁
            - ThreadLocal 变量使用后必须调用 remove() 防止内存泄漏

            【集合与字符串】
            - 字符串拼接在循环中使用 StringBuilder 而非 + 运算符
            - 集合初始化时指定初始容量（如 new ArrayList<>(expectedSize)）
            - 优先使用基本类型而非包装类型
            - Map.keySet() + get() 遍历应替换为 entrySet()

            【缓存与异步】
            - 频繁访问的静态数据应使用缓存
            - 非核心流程操作应异步化处理
            - 合理设置缓存过期时间和淘汰策略
            """;
    }
}
