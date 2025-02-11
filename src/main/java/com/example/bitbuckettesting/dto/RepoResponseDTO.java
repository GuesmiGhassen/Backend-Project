package com.example.bitbuckettesting.dto;

import lombok.Getter;

@Getter
public class RepoResponseDTO {
    private final String name;
    private final String fullName;
    private final boolean isPrivate;
    private final String description;
    private final String htmlUrl;

    public RepoResponseDTO(String name, String fullName, boolean isPrivate, String description, String htmlUrl) {
        this.name = name;
        this.fullName = fullName;
        this.isPrivate = isPrivate;
        this.description = description;
        this.htmlUrl = htmlUrl;
    }

    public boolean isPrivate() { return isPrivate; }
}
