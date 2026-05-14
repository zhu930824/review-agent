package com.review.agent.infrastructure.agent.skill;

import org.springframework.stereotype.Component;

@Component
public class JavaCodeStyleSkill implements Skill {

    @Override
    public String getName() { return "JavaCodeStyleSkill"; }

    @Override
    public String getDescription() { return "阿里巴巴 Java 开发手册代码风格规范"; }

    @Override
    public String getKnowledgeRules() {
        return """
            你是 Java 代码风格审查专家。请按以下规则审查：

            【命名规范】
            - 类名使用 UpperCamelCase（如 UserService、OrderDTO）
            - 方法名和变量名使用 lowerCamelCase（如 getUserById、orderList）
            - 常量使用 CONSTANT_CASE，全大写加下划线（如 MAX_RETRY_COUNT）
            - 包名统一小写，点分隔符之间有且仅有一个自然语义的单词（如 com.review.agent.service）
            - BO/VO/DTO 等 POJO 类名以对应后缀结尾

            【代码格式】
            - 大括号使用约定（左大括号不换行，右大括号换行）
            - 单行字符数限制不超过 120 个
            - 不同逻辑、不同语义、不同业务的代码之间插入空行分隔
            - 运算符左右各一个空格

            【注释规范】
            - 类、类属性、类方法必须使用 Javadoc 规范
            - 所有抽象方法必须用 Javadoc 注释
            - 方法内部单行注释使用 //，多行使用 /* */
            - 注释应说明设计思路而非复述代码

            【其他规范】
            - 魔法值（未经定义的常量）不应直接出现在代码中
            - equals 调用时将常量或确定有值的对象放在前面
            """;
    }
}
