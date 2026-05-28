package com.review.agent.infrastructure.memory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TeamMemoryStore {

    private final ObjectMapper objectMapper;
    private final Map<String, TeamMemory> store = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        List<TeamMemory> seeds = seedMemories();
        seeds.forEach(m -> store.put(m.getMemoryKey(), m));
        log.info("团队记忆已加载 {} 条", seeds.size());
    }

    public List<TeamMemory> listAll() {
        return new ArrayList<>(store.values());
    }

    public List<TeamMemory> listByCategory(String category) {
        return store.values().stream()
                .filter(m -> category.equalsIgnoreCase(m.getCategory()))
                .collect(Collectors.toList());
    }

    public TeamMemory get(String key) {
        return store.get(key);
    }

    public void save(TeamMemory memory) {
        if (memory.getCreatedAt() == null) {
            memory.setCreatedAt(LocalDateTime.now());
        }
        memory.setUpdatedAt(LocalDateTime.now());
        store.put(memory.getMemoryKey(), memory);
        log.info("团队记忆已保存: {}", memory.getMemoryKey());
    }

    private List<TeamMemory> seedMemories() {
        List<TeamMemory> list = new ArrayList<>();

        list.add(TeamMemory.builder()
                .memoryKey("mem-controller-no-db")
                .category("ARCHITECTURE")
                .title("Controller 禁止直连数据库")
                .content("所有 Controller 层代码必须通过 Service 层访问数据库，避免跨层依赖导致架构腐化")
                .tags(List.of("分层架构", "DDD", "依赖反转"))
                .source("团队架构评审")
                .severity("BLOCKER")
                .createdAt(LocalDateTime.now())
                .build());

        list.add(TeamMemory.builder()
                .memoryKey("mem-n-plus-one")
                .category("PERFORMANCE")
                .title("N+1 查询问题")
                .content("循环内执行数据库查询是最常见的技术债来源之一，应使用批量查询或 JOIN 语句替代")
                .tags(List.of("性能", "数据库", "循环"))
                .source("线上事故复盘")
                .severity("BLOCKER")
                .createdAt(LocalDateTime.now())
                .build());

        list.add(TeamMemory.builder()
                .memoryKey("mem-sensitive-log")
                .category("SECURITY")
                .title("敏感信息不可打印到日志")
                .content("用户手机号、身份证号、密码等敏感信息不得通过 log.info() 等日志打印，防止日志泄露")
                .tags(List.of("安全", "日志", "GDPR"))
                .source("安全审计")
                .severity("BLOCKER")
                .createdAt(LocalDateTime.now())
                .build());

        list.add(TeamMemory.builder()
                .memoryKey("mem-test-coverage")
                .category("QUALITY")
                .title("核心路径必须覆盖测试")
                .content("所有 Service 和 Domain 层的核心业务方法必须有单元测试覆盖，异常分支也必须覆盖")
                .tags(List.of("测试", "质量", "覆盖率"))
                .source("质量评审")
                .severity("MAJOR")
                .createdAt(LocalDateTime.now())
                .build());

        list.add(TeamMemory.builder()
                .memoryKey("mem-api-versioning")
                .category("ARCHITECTURE")
                .title("API 版本管理")
                .content("对外 API 变更必须做版本管理，禁止不兼容变更直接上线，需要向下兼容过渡期")
                .tags(List.of("API", "兼容性", "版本管理"))
                .source("线上故障复盘")
                .severity("MAJOR")
                .createdAt(LocalDateTime.now())
                .build());

        return list;
    }
}
