package com.review.agent.infrastructure.rule;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RuleRepository {

    private final ObjectMapper objectMapper;
    private final Map<String, RuleDefinition> rules = new ConcurrentHashMap<>();

    @PostConstruct
    void loadSeedRules() {
        internalRules().forEach(rule -> rules.put(rule.getRuleKey(), rule));
        loadFromFile();
        log.info("规则引擎已加载 {} 条规则", rules.size());
    }

    public List<RuleDefinition> listEnabled() {
        return rules.values().stream()
                .filter(r -> !Boolean.FALSE.equals(r.getEnabled()))
                .collect(Collectors.toList());
    }

    public List<RuleDefinition> listByCategory(String category) {
        return rules.values().stream()
                .filter(r -> category.equalsIgnoreCase(r.getCategory())
                        && !Boolean.FALSE.equals(r.getEnabled()))
                .collect(Collectors.toList());
    }

    public RuleDefinition get(String ruleKey) {
        return rules.get(ruleKey);
    }

    public void register(RuleDefinition rule) {
        rules.put(rule.getRuleKey(), rule);
    }

    private void loadFromFile() {
        try {
            ClassPathResource resource = new ClassPathResource("rules/seed-rules.json");
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    List<RuleDefinition> fileRules = objectMapper.readValue(is,
                            new TypeReference<List<RuleDefinition>>() {});
                    fileRules.forEach(rule -> rules.putIfAbsent(rule.getRuleKey(), rule));
                    log.info("从文件加载 {} 条规则", fileRules.size());
                }
            }
        } catch (Exception e) {
            log.warn("加载外部规则文件失败，使用内置规则: {}", e.getMessage());
        }
    }

    private List<RuleDefinition> internalRules() {
        List<RuleDefinition> list = new ArrayList<>();

        list.add(RuleDefinition.builder()
                .ruleKey("arch-no-controller-db")
                .name("禁止 Controller 直接访问数据库")
                .category("ARCHITECTURE")
                .severity("BLOCKER")
                .description("Controller 层不应直接持有 Mapper 或 Repository 引用")
                .message("Controller 直接访问数据库，违反分层架构原则")
                .suggestion("请通过 Service 层封装数据访问逻辑")
                .conditions(List.of(
                        RuleCondition.builder()
                                .type("PATTERN")
                                .pathPattern("**/controller/**")
                                .contentPattern("@Autowired\s+(\\w+Mapper|\\w+Repository)")
                                .build(),
                        RuleCondition.builder()
                                .type("PATTERN")
                                .pathPattern("**/controller/**")
                                .contentPattern("@Resource\s+private\s+(\\w+Mapper|\\w+Repository)")
                                .build()
                ))
                .enabled(true)
                .build());

        list.add(RuleDefinition.builder()
                .ruleKey("security-sql-injection")
                .name("禁止字符串拼接 SQL")
                .category("SECURITY")
                .severity("BLOCKER")
                .description("SQL 语句通过字符串拼接构造存在注入风险")
                .message("SQL 语句通过字符串拼接构造，存在 SQL 注入风险")
                .suggestion("使用参数化查询或 MyBatis #{} 占位符替代 ${} 和字符串拼接")
                .conditions(List.of(
                        RuleCondition.builder()
                                .type("PATTERN")
                                .pathPattern("**/*.java")
                                .contentPattern("\"(SELECT|INSERT|UPDATE|DELETE|select|insert|update|delete)\\s.*\"\\s*\\+")
                                .build()
                ))
                .enabled(true)
                .build());

        list.add(RuleDefinition.builder()
                .ruleKey("security-unsafe-deserialize")
                .name("禁止不安全的反序列化")
                .category("SECURITY")
                .severity("BLOCKER")
                .description("Java 原生反序列化可能导致远程代码执行")
                .message("使用了 ObjectInputStream.readObject()，存在反序列化漏洞风险")
                .suggestion("使用 Jackson 等安全的序列化/反序列化框架替代 Java 原生序列化")
                .conditions(List.of(
                        RuleCondition.builder()
                                .type("PATTERN")
                                .pathPattern("**/*.java")
                                .contentPattern("ObjectInputStream.*readObject\\(\\)")
                                .build()
                ))
                .enabled(true)
                .build());

        list.add(RuleDefinition.builder()
                .ruleKey("exception-no-empty-catch")
                .name("禁止空的异常捕获")
                .category("EXCEPTION_HANDLING")
                .severity("MAJOR")
                .description("空的 catch 块会吞掉异常，导致问题难以排查")
                .message("异常被静默吞掉，未做任何处理或日志记录")
                .suggestion("至少记录日志 log.error()，或重新抛出业务异常")
                .conditions(List.of(
                        RuleCondition.builder()
                                .type("PATTERN")
                                .pathPattern("**/*.java")
                                .contentPattern("catch\\s*\\(.*\\)\\s*\\{\\s*\\}")
                                .build()
                ))
                .enabled(true)
                .build());

        list.add(RuleDefinition.builder()
                .ruleKey("perf-no-to-string-in-log")
                .name("日志中禁止直接调用大对象 toString")
                .category("PERFORMANCE")
                .severity("MINOR")
                .description("直接 toString 会产生大量中间字符串对象")
                .message("日志中直接调用大对象 toString() 可能影响性能")
                .suggestion("使用占位符参数化日志输出，避免提前计算字符串")
                .conditions(List.of(
                        RuleCondition.builder()
                                .type("PATTERN")
                                .pathPattern("**/*.java")
                                .contentPattern("log\\.(info|debug|warn)\\(.*\\.toString\\(\\)")
                                .build()
                ))
                .enabled(true)
                .build());

        list.add(RuleDefinition.builder()
                .ruleKey("ddd-app-no-infra")
                .name("Application 层禁止依赖 Infrastructure")
                .category("ARCHITECTURE")
                .severity("BLOCKER")
                .description("Application Service/UseCase 不应直接引用 Infrastructure 包")
                .message("Application 层直接依赖 Infrastructure 层，违反 DDD 分层")
                .suggestion("通过接口进行依赖反转，Infrastructure 实现 Domain 定义的接口")
                .conditions(List.of(
                        RuleCondition.builder()
                                .type("PATTERN")
                                .pathPattern("**/application/**")
                                .contentPattern("import\s+.*\\.infrastructure\\.persistence\\.")
                                .build()
                ))
                .enabled(true)
                .build());

        list.add(RuleDefinition.builder()
                .ruleKey("perf-no-loop-db")
                .name("禁止循环中查询数据库")
                .category("PERFORMANCE")
                .severity("BLOCKER")
                .description("for/while 循环内不应直接调用 Mapper 查询方法")
                .message("循环内执行数据库查询，可能导致 N+1 问题")
                .suggestion("使用批量查询替代循环内的单次查询")
                .conditions(List.of(
                        RuleCondition.builder()
                                .type("PATTERN")
                                .pathPattern("**/*.java")
                                .contentPattern("(for\s*\\(|while\s*\\()[^)]*\\)\s*\\{[^}]*Mapper\\.(select|query|find)")
                                .build()
                ))
                .enabled(true)
                .build());

        list.add(RuleDefinition.builder()
                .ruleKey("security-plain-aksk")
                .name("禁止明文 AccessKey/SecretKey")
                .category("SECURITY")
                .severity("BLOCKER")
                .description("代码中不得硬编码 AK/SK、Token、密码等敏感凭证")
                .message("代码中存在疑似明文密钥（AK/SK/Token）")
                .suggestion("通过环境变量、配置中心或密钥管理服务获取凭证")
                .conditions(List.of(
                        RuleCondition.builder()
                                .type("PATTERN")
                                .pathPattern("**/*.java")
                                .contentPattern("(?i)(access_key|secret_key|accesskey|secretkey|accessToken)\s*=\s*\"[^\"]{16,}\"")
                                .build(),
                        RuleCondition.builder()
                                .type("PATTERN")
                                .pathPattern("**/*.java")
                                .contentPattern("(?i)(password|passwd)\s*=\s*\"[^\"]+\"")
                                .build()
                ))
                .enabled(true)
                .build());

        list.add(RuleDefinition.builder()
                .ruleKey("exception-no-print-stacktrace")
                .name("禁止 printStackTrace")
                .category("EXCEPTION_HANDLING")
                .severity("MAJOR")
                .description("生产代码不应使用 e.printStackTrace()，应使用日志框架")
                .message("使用了 printStackTrace()，生产代码应使用日志框架")
                .suggestion("替换为 log.error(\"context\", e)")
                .conditions(List.of(
                        RuleCondition.builder()
                                .type("PATTERN")
                                .pathPattern("**/*.java")
                                .contentPattern("\\.printStackTrace\\(\\)")
                                .build()
                ))
                .enabled(true)
                .build());

        list.add(RuleDefinition.builder()
                .ruleKey("code-no-system-out")
                .name("禁止 System.out/err 输出")
                .category("CODE_STYLE")
                .severity("MINOR")
                .description("生产代码应使用日志框架，禁止标准输出")
                .message("使用了 System.out 或 System.err 输出")
                .suggestion("使用 log.info()/log.error() 替代")
                .conditions(List.of(
                        RuleCondition.builder()
                                .type("PATTERN")
                                .pathPattern("**/java/**")
                                .contentPattern("System\\.(out|err)\\.print")
                                .build()
                ))
                .enabled(true)
                .build());

        list.add(RuleDefinition.builder()
                .ruleKey("perf-no-string-concat-loop")
                .name("循环中禁止字符串拼接")
                .category("PERFORMANCE")
                .severity("MAJOR")
                .description("for 循环内使用 += 拼接字符串会导致大量临时对象")
                .message("循环内使用 += 拼接字符串，大量临时对象将导致 GC 压力")
                .suggestion("使用 StringBuilder 或 String.join() 替代")
                .conditions(List.of(
                        RuleCondition.builder()
                                .type("PATTERN")
                                .pathPattern("**/*.java")
                                .contentPattern("(for\s*\\(|while\s*\\()[^)]*\\)\\s*\\{[^}]*\\+=")
                                .build()
                ))
                .enabled(true)
                .build());

        list.add(RuleDefinition.builder()
                .ruleKey("arch-public-field")
                .name("禁止公开实例字段")
                .category("ARCHITECTURE")
                .severity("MAJOR")
                .description("实体/服务类不应使用 public 非 static 字段")
                .message("类中存在 public 实例字段，破坏封装性")
                .suggestion("改为 private 字段并提供 getter/setter 或使用 Lombok")
                .conditions(List.of(
                        RuleCondition.builder()
                                .type("PATTERN")
                                .pathPattern("**/domain/**")
                                .contentPattern("public\s+(?!static|final)(\\w+)\s+\\w+;")
                                .build()
                ))
                .enabled(true)
                .build());

        return list;
    }
}
