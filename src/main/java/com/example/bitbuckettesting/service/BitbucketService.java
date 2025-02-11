package com.example.bitbuckettesting.service;

import com.example.bitbuckettesting.config.JsonBuilder;
import com.example.bitbuckettesting.config.OAuthConfig;
import com.example.bitbuckettesting.dto.BranchRequestDTO;
import com.example.bitbuckettesting.dto.FolderPushRequestDTO;
import com.example.bitbuckettesting.dto.RepoRequestDTO;
import com.example.bitbuckettesting.dto.RepoResponseDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BitbucketService {

    RestTemplate restTemplate = new RestTemplate();

    private final OAuthConfig oAuthConfig;

    @Value( "${bitbucket.repo-url}")
    private String RepoUrl;

    public ResponseEntity<List<RepoResponseDTO>> getRepositories(String workspace) {
        String accessToken = oAuthConfig.getAccessToken();
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        HttpHeaders headers = createHeaders(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = RepoUrl + workspace;
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
                List<RepoResponseDTO> repositories = new ArrayList<>();

                for (JsonNode repoNode : jsonNode.get("values")) {
                    try {
                        String name = repoNode.has("name") ? repoNode.get("name").asText() : "Unknown";
                        String fullName = repoNode.has("full_name") ? repoNode.get("full_name").asText() : "";
                        boolean isPrivate = repoNode.has("is_private") && repoNode.get("is_private").asBoolean();
                        String description = repoNode.has("description") && !repoNode.get("description").isNull() ?
                                repoNode.get("description").asText() : "";
                        String htmlUrl = repoNode.has("links") && repoNode.get("links").has("html") && repoNode.get("links").get("html").has("href") ?
                                repoNode.get("links").get("html").get("href").asText() : "";
                        repositories.add(new RepoResponseDTO(name, fullName, isPrivate, description, htmlUrl));
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
                return ResponseEntity.ok(repositories);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        }
        return ResponseEntity.status(response.getStatusCode()).body(null);
    }

    public ResponseEntity<RepoResponseDTO> createRepository(RepoRequestDTO request, String workspace) {
        String accessToken = oAuthConfig.getAccessToken();
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        HttpHeaders headers = createHeaders(accessToken);

        ObjectNode requestBody = JsonBuilder.createRepositoryJson(request, workspace);
        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

        ResponseEntity<ObjectNode> response = restTemplate.exchange(
                RepoUrl + workspace + "/" + request.getName(),
                HttpMethod.POST,
                entity,
                ObjectNode.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            ObjectNode responseBody = response.getBody();
            return ResponseEntity.ok(new RepoResponseDTO(
                    responseBody.get("name").asText(),
                    responseBody.get("full_name").asText(),
                    responseBody.get("is_private").asBoolean(),
                    responseBody.get("description").asText(),
                    responseBody.get("links").get("html").get("href").asText()
            ));
        }
        return ResponseEntity.status(response.getStatusCode()).body(null);
    }

    public String deleteRepository(String workspace, String repo_slug) {
        String accessToken = oAuthConfig.getAccessToken();

        HttpHeaders headers = createHeaders(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                RepoUrl + workspace + "/" + repo_slug,
                HttpMethod.DELETE,
                entity,
                String.class
        );
        if (response.getStatusCode().is2xxSuccessful()) {
            return "Repository " + repo_slug + " deleted successfully.";
        }
        return "Error: " + response.getBody();
    }

    public ResponseEntity<String> createBranch(String workspace, String repo_slug, BranchRequestDTO request) {
        String accessToken = oAuthConfig.getAccessToken();
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: Unauthorized. Access token is missing.");
        }

        HttpHeaders headers = createHeaders(accessToken);
        HttpEntity<BranchRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                RepoUrl + workspace + "/" + repo_slug + "/refs/branches",
                HttpMethod.POST,
                entity,
                String.class
        );
        if (response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.ok("Branch '" + request.getName() + "' created successfully in repository '" + repo_slug + "'.");
        }
        return ResponseEntity.status(response.getStatusCode()).body("Error: " + response.getBody());
    }

    public ResponseEntity<String> deleteBranch(String workspace, String repo_slug, String branch) {
        String accessToken = oAuthConfig.getAccessToken();
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: Unauthorized. Access token is missing.");
        }
        HttpHeaders headers = createHeaders(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                RepoUrl + workspace + "/" + repo_slug + "/refs/branches/" + branch,
                HttpMethod.DELETE,
                entity,
                String.class
        );
        if (response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.ok("Branch '" + branch + "' deleted successfully from repository '" + repo_slug + "'.");
        }
        return ResponseEntity.status(response.getStatusCode()).body("Error: " + response.getBody());
    }

    public ResponseEntity<String> pushFolderToRepo(String workspace, String repo_slug, FolderPushRequestDTO request) {
        String accessToken = oAuthConfig.getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: Unauthorized. Access token is missing.");
        }

        File folder = new File(request.getPath());
        if (!folder.exists() || !folder.isDirectory()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Invalid folder path.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("branch", request.getBranch());
        body.add("message", request.getCommitMessage());

        addFilesRecursively(folder, body, folder.getAbsolutePath());

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                RepoUrl + workspace + "/" + repo_slug + "/src",
                HttpMethod.POST,
                entity,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.ok("Folder pushed successfully to repository '" + repo_slug + "' on branch '" + request.getBranch() + "'.");
        }

        return ResponseEntity.status(response.getStatusCode()).body("Error: " + response.getBody());
    }

    private void addFilesRecursively(File file, MultiValueMap<String, Object> body, String rootPath) {
        if (file.isFile()) {
            try {
                final String relativePath = file.getAbsolutePath().replace(rootPath, "").replace("\\", "/");
                final String cleanPath = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;

                body.add(cleanPath, new ByteArrayResource(Files.readAllBytes(file.toPath())) {
                    @Override
                    public String getFilename() {
                        return cleanPath;
                    }
                });
            } catch (IOException e) {
                System.out.println("[ERROR] Could not read file: " + file.getName());
            }
        } else if (file.isDirectory()) {
            for (File child : Objects.requireNonNull(file.listFiles())) {
                addFilesRecursively(child, body, rootPath);
            }
        }
    }

    private HttpHeaders createHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

}
