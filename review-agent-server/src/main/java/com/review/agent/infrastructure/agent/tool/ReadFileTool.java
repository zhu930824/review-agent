package com.review.agent.infrastructure.agent.tool;

import com.review.agent.domain.entity.Project;
import com.review.agent.infrastructure.persistence.ProjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReadFileTool {

    private final ProjectMapper projectMapper;

    private static final int MAX_CHARS = 3000;

    @Tool(description = "读取项目仓库中当前HEAD版本指定文件的完整内容，用于获取变更文件的上下文代码")
    public String readFile(
            @ToolParam(description = "项目ID") Long projectId,
            @ToolParam(description = "文件路径") String filePath) {
        Project project = projectMapper.selectById(projectId);
        if (project == null || project.getLocalPath() == null) {
            return "项目不存在或仓库未克隆";
        }
        try {
            Path fullPath = Path.of(project.getLocalPath(), filePath);
            if (!Files.exists(fullPath)) {
                return "文件不存在: " + filePath;
            }
            String content = Files.readString(fullPath);
            if (content.length() > MAX_CHARS) {
                return content.substring(0, MAX_CHARS)
                        + "\n... (内容已截断，共" + content.length() + "字符)";
            }
            return content;
        } catch (IOException e) {
            log.error("读取文件失败: projectId={}, file={}", projectId, filePath, e);
            return "读取失败: " + e.getMessage();
        }
    }
}
