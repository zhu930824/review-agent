package com.review.agent.infrastructure.gateway;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiGateway {

    private final Map<String, PromptTemplate> prompts = new ConcurrentHashMap<>();
    private final List<ModelCallRecord> auditLog = Collections.synchronizedList(new ArrayList<>());

    @PostConstruct
    void init() {
        seedPrompts().forEach(p -> prompts.put(p.getTemplateKey(), p));
        log.info("AI Gateway 已加载 {} 个 Prompt 模板", prompts.size());
    }

    public PromptTemplate resolvePrompt(String templateKey, Map<String, String> variables) {
        PromptTemplate template = prompts.get(templateKey);
        if (template == null) {
            log.warn("Prompt 模板不存在: {}", templateKey);
            return null;
        }
        String resolved = template.getContent();
        if (variables != null && resolved != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                resolved = resolved.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }
        }
        return PromptTemplate.builder()
                .templateKey(template.getTemplateKey())
                .name(template.getName())
                .content(resolved)
                .version(template.getVersion())
                .build();
    }

    public List<PromptTemplate> listPrompts() {
        return new ArrayList<>(prompts.values());
    }

    public PromptTemplate getPrompt(String key) {
        return prompts.get(key);
    }

    public void recordCall(ModelCallRecord record) {
        if (record.getStartedAt() == null) {
            record.setStartedAt(LocalDateTime.now());
        }
        if (record.getCompletedAt() == null) {
            record.setCompletedAt(LocalDateTime.now());
        }
        auditLog.add(record);
        if (auditLog.size() > 1000) {
            auditLog.subList(0, 500).clear();
        }
        log.debug("模型调用记录: model={}, tokens={}, latency={}ms",
                record.getModelName(), record.getPromptTokens(), record.getLatencyMs());
    }

    public List<ModelCallRecord> getRecentCalls(String status, int limit) {
        return auditLog.stream()
                .filter(r -> status == null || status.equals(r.getStatus()))
                .sorted((a, b) -> b.getStartedAt().compareTo(a.getStartedAt()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public ModelCallStats getStats() {
        int total = auditLog.size();
        long totalTokens = auditLog.stream()
                .mapToLong(r -> (r.getPromptTokens() != null ? r.getPromptTokens() : 0)
                        + (r.getCompletionTokens() != null ? r.getCompletionTokens() : 0))
                .sum();
        long failed = auditLog.stream().filter(r -> "FAILED".equals(r.getStatus())).count();
        double avgLatency = auditLog.stream()
                .mapToInt(r -> r.getLatencyMs() != null ? r.getLatencyMs() : 0)
                .average().orElse(0);

        return new ModelCallStats(total, totalTokens, failed, avgLatency);
    }

    public record ModelCallStats(long totalCalls, long totalTokens, long failedCalls, double avgLatencyMs) {}

    private List<PromptTemplate> seedPrompts() {
        return List.of(
                PromptTemplate.builder()
                        .templateKey("code-review-default")
                        .name("默认代码审查 Prompt")
                        .category("REVIEW")
                        .content("请审查以下 {{language}} 代码变更，关注代码规范、潜在缺陷、性能和安全问题。\n\n变更文件: {{filePath}}\n\n```{{language}}\n{{diffContent}}\n```")
                        .variables("language,filePath,diffContent")
                        .version("1.0.0")
                        .createdAt(LocalDateTime.now())
                        .build(),
                PromptTemplate.builder()
                        .templateKey("code-review-security")
                        .name("安全审查 Prompt")
                        .category("SECURITY")
                        .content("请以安全审计师的角色审查以下代码，重点关注:\n1. 注入漏洞 (SQL/命令/表达式)\n2. 敏感信息泄露\n3. 不安全的反序列化\n4. 权限校验缺失\n\n变更内容:\n```\n{{diffContent}}\n```")
                        .variables("diffContent")
                        .version("1.0.0")
                        .createdAt(LocalDateTime.now())
                        .build(),
                PromptTemplate.builder()
                        .templateKey("code-review-performance")
                        .name("性能审查 Prompt")
                        .category("PERFORMANCE")
                        .content("请审查以下代码的性能问题:\n1. N+1 查询\n2. 大对象频繁创建\n3. 锁竞争\n4. IO 阻塞\n\n变更文件: {{filePath}}\n\n```\n{{diffContent}}\n```")
                        .variables("filePath,diffContent")
                        .version("1.0.0")
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }
}
