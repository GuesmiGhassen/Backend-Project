package com.example.bitbuckettesting.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class BranchRequestDTO {
    private String name;
    @JsonProperty("target")
    private Target target;

    public BranchRequestDTO() {}
    public BranchRequestDTO(String name, String hash) {
        this.name = name;
        this.target = new Target(hash);
    }
    @Getter
    @Setter
    public static class Target {
        private String hash;

        public Target() {}
        public Target(String hash) {
            this.hash = hash;
        }
    }
}
