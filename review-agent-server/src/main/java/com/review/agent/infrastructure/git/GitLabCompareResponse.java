package com.review.agent.infrastructure.git;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * GitLab Repository Compare API 返回结构。
 *
 * @see <a href="https://docs.gitlab.com/ee/api/repositories.html#compare-branches-tags-or-commits">GitLab API 文档</a>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitLabCompareResponse {

    private Commit commit;

    private List<Commit> commits;

    private List<Diff> diffs;

    @JsonProperty("compare_timeout")
    private Boolean compareTimeout;

    @JsonProperty("compare_same_ref")
    private Boolean compareSameRef;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Commit {
        private String id;
        @JsonProperty("short_id")
        private String shortId;
        private String title;
        private String message;
        @JsonProperty("author_name")
        private String authorName;
        @JsonProperty("created_at")
        private String createdAt;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Diff {
        /**
         * Unified diff 文本，可直接交由 DiffParser 解析。
         */
        private String diff;

        @JsonProperty("new_path")
        private String newPath;

        @JsonProperty("old_path")
        private String oldPath;

        @JsonProperty("a_mode")
        private String aMode;

        @JsonProperty("b_mode")
        private String bMode;

        @JsonProperty("new_file")
        private Boolean newFile;

        @JsonProperty("renamed_file")
        private Boolean renamedFile;

        @JsonProperty("deleted_file")
        private Boolean deletedFile;
    }
}
