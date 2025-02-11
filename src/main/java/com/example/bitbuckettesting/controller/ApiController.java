package com.example.bitbuckettesting.controller;

import com.example.bitbuckettesting.config.OAuthConfig;
import com.example.bitbuckettesting.dto.BranchRequestDTO;
import com.example.bitbuckettesting.dto.FolderPushRequestDTO;
import com.example.bitbuckettesting.dto.RepoRequestDTO;
import com.example.bitbuckettesting.dto.RepoResponseDTO;
import com.example.bitbuckettesting.service.BitbucketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {
    @Autowired
    private OAuthConfig oAuthConfig;
    @Autowired
    private BitbucketService bitbucketService;

    @GetMapping("/token")
    public String getToken() {
        return oAuthConfig.getAccessToken();
    }

    @GetMapping("/repositories/{workspace}")
    public ResponseEntity<List<RepoResponseDTO>> getRepositories(
            @PathVariable String workspace
    ) {
        return bitbucketService.getRepositories(workspace);
    }

    @PostMapping("/repository/{workspace}")
    public ResponseEntity<RepoResponseDTO> createRepository(
            @PathVariable String workspace,
            @RequestBody RepoRequestDTO request
    ) {
        return bitbucketService.createRepository(request, workspace);
    }

    @DeleteMapping("/repository/{workspace}/{repo_slug}")
    public String deleteRepository(
            @PathVariable String workspace,
            @PathVariable String repo_slug
    ) {
        return bitbucketService.deleteRepository(workspace, repo_slug);
    }

    @PostMapping("/repository/{workshop}/{repo_slug}/branch")
    public ResponseEntity<String> createBranch(
            @PathVariable String workshop,
            @PathVariable String repo_slug,
            @RequestBody BranchRequestDTO request
    ) {
        return bitbucketService.createBranch(workshop, repo_slug, request);
    }
    @DeleteMapping("/repository/{workshop}/{repo_slug}/branch/{branch}")
    public ResponseEntity<String> deleteBranch(
            @PathVariable String workshop,
            @PathVariable String repo_slug,
            @PathVariable String branch
    ){
        return bitbucketService.deleteBranch(workshop, repo_slug, branch);
    }

    @PostMapping("/push/{workspace}/{repo_slug}")
    public ResponseEntity<String> pushFolderToRepo(
            @PathVariable String workspace,
            @PathVariable String repo_slug,
            @RequestBody FolderPushRequestDTO request) {
        return bitbucketService.pushFolderToRepo(workspace, repo_slug, request);
    }
}
