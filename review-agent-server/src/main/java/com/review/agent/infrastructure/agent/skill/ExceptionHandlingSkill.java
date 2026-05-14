package com.review.agent.infrastructure.agent.skill;

import org.springframework.stereotype.Component;

@Component
public class ExceptionHandlingSkill implements Skill {

    @Override
    public String getName() { return "ExceptionHandlingSkill"; }

    @Override
    public String getDescription() { return "异常处理规范检测规则"; }

    @Override
    public String getKnowledgeRules() {
        return """
            你是异常处理审查专家。请按以下规则审查：

            【异常捕获】
            - 禁止捕获 Throwable 或 Exception 顶层异常而不做任何处理（异常吞没）
            - catch 块中必须记录日志或重新抛出，不可留空
            - 禁止在 finally 块中抛出异常或执行 return 语句
            - 精确捕获具体异常类型，而非泛化的 Exception

            【异常传播】
            - 不应将异常信息暴露给最终用户（返回原始异常堆栈）
            - 业务异常应使用自定义异常类，携带明确的错误码和消息
            - RuntimeException 无需在方法签名中声明 throws

            【资源关闭】
            - I/O 流、数据库连接必须在 finally 或 try-with-resources 中关闭
            - 多个资源的关闭应分别 try-catch，避免一个关闭失败影响其他

            【空指针防护】
            - 方法返回值可能为 null 时应明确标注 @Nullable
            - 对外暴露的方法入参应进行非空校验
            - Optional 用于可能为空的返回值，而非作为方法参数
            """;
    }
}
