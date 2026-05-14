package com.review.agent.infrastructure.agent.skill;

import org.springframework.stereotype.Component;

@Component
public class SecuritySkill implements Skill {

    @Override
    public String getName() { return "SecuritySkill"; }

    @Override
    public String getDescription() { return "代码安全漏洞检测规则"; }

    @Override
    public String getKnowledgeRules() {
        return """
            你是代码安全审计专家。请按以下规则审查：

            【注入防护】
            - SQL 查询必须使用参数化查询（PreparedStatement / MyBatis #{}），禁止拼接 SQL 字符串
            - 动态排序字段必须使用白名单校验，禁止直接拼接 ORDER BY 值
            - XML/JSON 解析时禁用外部实体注入（XXE）

            【XSS 防护】
            - 用户输入输出到页面时必须进行 HTML 转义
            - 富文本内容使用白名单过滤（如 OWASP AntiSamy）
            - Content-Type 响应头正确设置 charset

            【认证与授权】
            - 禁止硬编码密码、密钥、Token
            - 敏感配置必须通过环境变量或密钥管理服务读取
            - 接口必须进行权限校验，禁用未授权访问
            - 密码必须使用不可逆加密算法存储（bcrypt/scrypt/PBKDF2）

            【敏感信息保护】
            - 日志中禁止打印密码、手机号、身份证等敏感信息
            - API 响应中避免返回数据库异常堆栈
            - CSRF Token 机制对状态变更操作生效
            """;
    }
}
