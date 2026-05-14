package com.review.agent.infrastructure.git;

import com.review.agent.domain.dto.diff.DiffLine;
import com.review.agent.domain.dto.diff.FileChange;
import com.review.agent.domain.dto.diff.Hunk;
import com.review.agent.domain.dto.diff.LineType;
import com.review.agent.domain.enums.ChangeType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析 unified diff 输出，转为结构化的 FileChange 列表。
 * 无状态组件，线程安全。
 */
@Component
public class DiffParser {

    private static final Pattern DIFF_HEADER = Pattern.compile("^diff --git a/(.*) b/(.*)$");
    private static final Pattern OLD_FILE = Pattern.compile("^--- (?:a/(.+)|/dev/null)$");
    private static final Pattern NEW_FILE = Pattern.compile("^\\+\\+\\+ (?:b/(.+)|/dev/null)$");
    private static final Pattern HUNK_HEADER = Pattern.compile(
            "^@@\\s+-(\\d+)(?:,(\\d+))?\\s+\\+(\\d+)(?:,(\\d+))?\\s+@@");
    private static final Pattern RENAME_FROM = Pattern.compile("^rename from (.+)$");

    public List<FileChange> parse(String diffOutput) {
        if (diffOutput == null || diffOutput.isBlank()) {
            return List.of();
        }

        String[] fileSections = diffOutput.split("(?=diff --git )");
        List<FileChange> result = new ArrayList<>(fileSections.length);

        for (String section : fileSections) {
            if (section.isBlank()) {
                continue;
            }
            FileChange fileChange = parseFileSection(section.trim());
            if (fileChange != null) {
                result.add(fileChange);
            }
        }
        return result;
    }

    private FileChange parseFileSection(String section) {
        String[] lines = section.split("\n");
        if (lines.length == 0) {
            return null;
        }

        String oldPath = null;
        String newPath = null;
        Matcher headerMatcher = DIFF_HEADER.matcher(lines[0]);
        if (headerMatcher.matches()) {
            oldPath = headerMatcher.group(1);
            newPath = headerMatcher.group(2);
        }

        boolean isAdd = false;
        boolean isDelete = false;
        boolean isRename = false;
        int totalAdditions = 0;
        int totalDeletions = 0;

        List<Hunk> hunks = new ArrayList<>();
        // 当前 hunk 的累积状态
        List<DiffLine> currentDiffLines = null;
        StringBuilder currentHunkContent = null;
        int hunkOldStart = 0;
        int hunkOldLines = 0;
        int hunkNewStart = 0;
        int hunkNewLines = 0;
        // 逐行递增的行号追踪器
        int oldLine = 0;
        int newLine = 0;

        for (String line : lines) {
            Matcher oldFileMatcher = OLD_FILE.matcher(line);
            if (oldFileMatcher.matches()) {
                String matched = oldFileMatcher.group(1);
                if (matched == null) {
                    isAdd = true;
                } else {
                    oldPath = matched;
                }
                continue;
            }

            Matcher newFileMatcher = NEW_FILE.matcher(line);
            if (newFileMatcher.matches()) {
                String matched = newFileMatcher.group(1);
                if (matched == null) {
                    isDelete = true;
                } else {
                    newPath = matched;
                }
                continue;
            }

            Matcher renameMatcher = RENAME_FROM.matcher(line);
            if (renameMatcher.matches()) {
                isRename = true;
                continue;
            }

            Matcher hunkMatcher = HUNK_HEADER.matcher(line);
            if (hunkMatcher.find()) {
                // 保存前一个 hunk
                if (currentHunkContent != null) {
                    hunks.add(buildHunk(hunkOldStart, hunkOldLines, hunkNewStart, hunkNewLines,
                            currentHunkContent, currentDiffLines));
                }
                hunkOldStart = Integer.parseInt(hunkMatcher.group(1));
                hunkOldLines = parseOptionalInt(hunkMatcher.group(2), 1);
                hunkNewStart = Integer.parseInt(hunkMatcher.group(3));
                hunkNewLines = parseOptionalInt(hunkMatcher.group(4), 1);
                oldLine = hunkOldStart;
                newLine = hunkNewStart;
                currentHunkContent = new StringBuilder(line);
                currentDiffLines = new ArrayList<>();
                continue;
            }

            // hunk 内容行：解析 DiffLine
            if (currentHunkContent != null) {
                currentHunkContent.append('\n').append(line);
                DiffLine diffLine = parseDiffLine(line, oldLine, newLine);
                currentDiffLines.add(diffLine);
                if (diffLine.getType() == LineType.ADDED) {
                    newLine++;
                    totalAdditions++;
                } else if (diffLine.getType() == LineType.REMOVED) {
                    oldLine++;
                    totalDeletions++;
                } else if (diffLine.getType() == LineType.CONTEXT) {
                    oldLine++;
                    newLine++;
                }
            }
        }

        // 保存最后一个 hunk
        if (currentHunkContent != null) {
            hunks.add(buildHunk(hunkOldStart, hunkOldLines, hunkNewStart, hunkNewLines,
                    currentHunkContent, currentDiffLines));
        }

        ChangeType changeType = resolveChangeType(isAdd, isDelete, isRename);

        return FileChange.builder()
                .filePath(newPath)
                .changeType(changeType)
                .oldFilePath(oldPath)
                .hunks(hunks)
                .additions(totalAdditions)
                .deletions(totalDeletions)
                .build();
    }

    /**
     * 根据 diff 行前缀识别行类型，跳过 hunk 头和无前缀的元数据行
     */
    private DiffLine parseDiffLine(String line, int oldLineNum, int newLineNum) {
        if (line.startsWith("+")) {
            return DiffLine.builder()
                    .type(LineType.ADDED)
                    .content(line.substring(1))
                    .lineNumber(newLineNum)
                    .build();
        }
        if (line.startsWith("-")) {
            return DiffLine.builder()
                    .type(LineType.REMOVED)
                    .content(line.substring(1))
                    .lineNumber(oldLineNum)
                    .build();
        }
        // 空格前缀为上下文行，其他（如 No newline 等）也归为上下文
        String content = line.startsWith(" ") ? line.substring(1) : line;
        return DiffLine.builder()
                .type(LineType.CONTEXT)
                .content(content)
                .lineNumber(oldLineNum)
                .build();
    }

    private Hunk buildHunk(int oldStart, int oldLines, int newStart, int newLines,
                           StringBuilder content, List<DiffLine> diffLines) {
        return Hunk.builder()
                .oldStart(oldStart)
                .oldLines(oldLines)
                .newStart(newStart)
                .newLines(newLines)
                .content(content.toString())
                .lines(diffLines)
                .build();
    }

    private ChangeType resolveChangeType(boolean isAdd, boolean isDelete, boolean isRename) {
        if (isRename) {
            return ChangeType.RENAME;
        }
        if (isAdd) {
            return ChangeType.ADD;
        }
        if (isDelete) {
            return ChangeType.DELETE;
        }
        return ChangeType.MODIFY;
    }

    private int parseOptionalInt(String value, int defaultValue) {
        return (value != null && !value.isEmpty()) ? Integer.parseInt(value) : defaultValue;
    }
}
