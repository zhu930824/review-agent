package com.review.agent.infrastructure.agent.tool;

import com.review.agent.domain.entity.Project;
import com.review.agent.infrastructure.persistence.ProjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CodeStructureTool {

    private final ProjectMapper projectMapper;

    private static final int MAX_DEPTH = 3;

    @Tool(description = "解析项目中的 Java 包结构和类依赖关系，返回目录树")
    public String analyzeStructure(
            @ToolParam(description = "项目ID") Long projectId,
            @ToolParam(description = "目标目录路径（相对于项目根目录）") String directory) {
        Project project = projectMapper.selectById(projectId);
        if (project == null || project.getLocalPath() == null) {
            return "项目不存在或仓库未克隆";
        }

        String targetDir = directory != null ? directory : "";
        File dir = new File(project.getLocalPath(), targetDir);
        if (!dir.exists() || !dir.isDirectory()) {
            return "目录不存在: " + directory;
        }

        List<String> tree = new ArrayList<>();
        buildTree(dir, "", tree, MAX_DEPTH);
        return String.join("\n", tree);
    }

    private void buildTree(File dir, String prefix, List<String> result, int maxDepth) {
        if (maxDepth <= 0) {
            result.add(prefix + "... (深度已达上限)");
            return;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.getName().startsWith(".")) {
                continue;
            }
            if (file.isDirectory()) {
                result.add(prefix + "+-- " + file.getName() + "/");
                buildTree(file, prefix + "|   ", result, maxDepth - 1);
            } else if (file.getName().endsWith(".java")) {
                result.add(prefix + "|-- " + file.getName());
            }
        }
    }
}
