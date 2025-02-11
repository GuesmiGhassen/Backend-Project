package com.example.bitbuckettesting.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class RepoRequestDTO {
    @Setter
    @Getter
    private String name;
    @Setter
    @Getter
    private String description;
    @JsonProperty("is_private")
    private boolean isPrivate;
    @Getter
    private final boolean hasIssues = true;
    @Getter
    private final boolean hasWiki = true;
    @Getter
    private final String forkPolicy = "allow_forks";

    public RepoRequestDTO() {}

    public RepoRequestDTO(String name, String description, boolean isPrivate) {
        this.name = name;
        this.description = description;
        this.isPrivate = isPrivate;
    }

    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean isPrivate) { this.isPrivate = isPrivate; }
}
