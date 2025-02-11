package com.example.bitbuckettesting.service;

import com.example.bitbuckettesting.config.JsonBuilder;
import com.example.bitbuckettesting.config.OAuthConfig;
import com.example.bitbuckettesting.dto.RepoRequestDTO;
import com.example.bitbuckettesting.dto.RepoResponseDTO;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import static org.junit.Assert.*;

import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class BitbucketServiceTest {

    @InjectMocks
    private BitbucketService bitbucketService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private OAuthConfig oAuthConfig;

    private static final String WORKSPACE = "bitbucket_testing2";

    @Test
    public void testGetRepositories_Success() {
        String accessToken = "mockAccessToken";
        Mockito.when(oAuthConfig.getAccessToken()).thenReturn(accessToken);

        String jsonResponse = "{ \"values\": " +
                "[ { \"name\": \"auth_repo\", " +
                "\"full_name\": \"bitbucket_testing2/auth_repo\", " +
                "\"is_private\": true, " +
                "\"description\": \"\", " +
                "\"links\": { \"html\": " +
                "{ \"href\": \"https://bitbucket.org/bitbucket_testing2/auth_repo\" } } } ] " +
                "}";
        ResponseEntity<String> mockResponse = new ResponseEntity<>(jsonResponse, HttpStatus.OK);

        Mockito.when(restTemplate.exchange(
                Mockito.anyString(),
                Mockito.eq(HttpMethod.GET),
                Mockito.any(HttpEntity.class),
                Mockito.eq(String.class)
        )).thenReturn(mockResponse);
        ResponseEntity<List<RepoResponseDTO>> response = bitbucketService.getRepositories(WORKSPACE);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        RepoResponseDTO repo = response.getBody().get(0);
        assertEquals("auth_repo", repo.getName());
        assertEquals("bitbucket_testing2/auth_repo", repo.getFullName());
        assertTrue(repo.isPrivate());
        assertEquals("", repo.getDescription());
        assertEquals("https://bitbucket.org/bitbucket_testing2/auth_repo", repo.getHtmlUrl());
    }

    @Test
    public void testGetRepositories_Unauthorized() {
        Mockito.when(oAuthConfig.getAccessToken()).thenReturn(null);

        ResponseEntity<List<RepoResponseDTO>> response = bitbucketService.getRepositories(WORKSPACE);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void testGetRepositories_BitbucketError() {
        Mockito.when(oAuthConfig.getAccessToken()).thenReturn("mockAccessToken");

        ResponseEntity<String> mockResponse = new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST);
        Mockito.when(restTemplate.exchange(
                Mockito.anyString(),
                Mockito.eq(HttpMethod.GET),
                Mockito.any(HttpEntity.class),
                Mockito.eq(String.class)
        )).thenReturn(mockResponse);
        ResponseEntity<List<RepoResponseDTO>> response = bitbucketService.getRepositories(WORKSPACE);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
