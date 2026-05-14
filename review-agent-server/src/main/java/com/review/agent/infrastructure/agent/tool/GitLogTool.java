package com.review.agent.infrastructure.agent.tool;

import com.review.agent.domain.entity.Project;
import com.review.agent.infrastructure.persistence.ProjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitLogTool {

    private final ProjectMapper projectMapper;

    @Tool(description = "查询指定项目中指定文件的 Git 提交历史，返回最近的 commit 记录")
    public String gitLog(
            @ToolParam(description = "项目ID") Long projectId,
            @ToolParam(description = "文件路径") String filePath) {
        Project project = projectMapper.selectById(projectId);
        if (project == null || project.getLocalPath() == null) {
            return "项目不存在或仓库未克隆";
        }
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "git", "log", "--oneline", "-n", "5", "--", filePath);
            pb.directory(new File(project.getLocalPath()));
            InputStream is = pb.start().getInputStream();
            String result = new String(is.readAllBytes());
            return result.isEmpty() ? "该文件无提交历史" : result;
        } catch (Exception e) {
            log.error("git log 失败: projectId={}, file={}", projectId, filePath, e);
            return "查询失败: " + e.getMessage();
        }
    }
}
