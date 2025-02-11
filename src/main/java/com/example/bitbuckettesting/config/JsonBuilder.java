package com.example.bitbuckettesting.config;

import com.example.bitbuckettesting.dto.RepoRequestDTO;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonBuilder {
    public static ObjectNode createRepositoryJson(RepoRequestDTO request, String workspace) {
        JsonNodeFactory jnf = JsonNodeFactory.instance;
        ObjectNode payload = jnf.objectNode();
        payload.put("scm", "git");
        payload.put("name", request.getName());
        payload.put("full_name", workspace + "/" + request.getName());
        payload.put("description", request.getDescription());
        payload.put("is_private", request.isPrivate());
        payload.put("has_issues", request.isHasIssues());
        payload.put("has_wiki", request.isHasWiki());
        payload.put("fork_policy", request.getForkPolicy());

        // Links
        ObjectNode links = payload.putObject("links");
        links.putObject("html").put("href", "https://bitbucket.org/" + workspace + "/" + request.getName());

        return payload;
    }
}
