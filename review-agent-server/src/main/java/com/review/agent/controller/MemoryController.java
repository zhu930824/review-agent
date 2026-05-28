package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.infrastructure.memory.TeamMemory;
import com.review.agent.infrastructure.memory.TeamMemoryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/memory")
@RequiredArgsConstructor
public class MemoryController {

    private final TeamMemoryStore memoryStore;

    @GetMapping
    public Result<List<TeamMemory>> listMemories(
            @RequestParam(value = "category", required = false) String category) {
        if (category != null) {
            return Result.success(memoryStore.listByCategory(category));
        }
        return Result.success(memoryStore.listAll());
    }

    @GetMapping("/{memoryKey}")
    public Result<TeamMemory> getMemory(@PathVariable("memoryKey") String memoryKey) {
        TeamMemory memory = memoryStore.get(memoryKey);
        if (memory == null) {
            return Result.fail(404, "团队记忆不存在: " + memoryKey);
        }
        return Result.success(memory);
    }

    @PostMapping
    public Result<TeamMemory> saveMemory(@RequestBody TeamMemory memory) {
        memoryStore.save(memory);
        return Result.success(memory);
    }
}
