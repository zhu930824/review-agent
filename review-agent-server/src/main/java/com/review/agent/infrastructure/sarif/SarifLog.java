package com.review.agent.infrastructure.sarif;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SarifLog {
    private String version;
    @JsonProperty("$schema")
    private String schema;
    private List<Run> runs;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Run {
        private Tool tool;
        private List<Result> results;
        @JsonProperty("columnKind")
        private String columnKind;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Tool {
        private Driver driver;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Driver {
        private String name;
        private String informationUri;
        private String version;
        private String organization;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Result {
        private String ruleId;
        private Integer ruleIndex;
        private String level;
        private Message message;
        private List<Location> locations;
        private String kind;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Message {
        private String text;
        private String markdown;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Location {
        private PhysicalLocation physicalLocation;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PhysicalLocation {
        private ArtifactLocation artifactLocation;
        private Region region;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ArtifactLocation {
        private String uri;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Region {
        private Integer startLine;
        private Integer endLine;
        private Integer startColumn;
        private Integer endColumn;
    }
}
