package com.example.bitbuckettesting.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FolderPushRequestDTO {
    private String branch;
    private String commitMessage;
    private String path;

    public FolderPushRequestDTO(String branch, String commitMessage, String path) {
        this.branch = branch;
        this.commitMessage = commitMessage;
        this.path = path;
    }
}
