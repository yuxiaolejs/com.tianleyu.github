package com.tianleyu.github;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class GitHubAppTest {

    private JwtProvider jwtProvider;
    private HttpClient client;
    private GitHubApp gitHubApp;

    @BeforeEach
    void setUp() {
        jwtProvider = mock(JwtProvider.class);
        client = mock(HttpClient.class);
        // gitHubApp = new GitHubApp(jwtProvider, client);
    }

    @Test
    void testAppInfo() throws Exception {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.body()).thenReturn("{\"name\":\"GitHub App\"}");

        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.get(anyString(), anyString(), any(HttpClient.class)))
                       .thenReturn(response);

            JSONObject appInfo = gitHubApp.appInfo();

            assertEquals("GitHub App", appInfo.getString("name"));
        }
    }

    @Test
    void testOrg() throws Exception {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("{\"id\": 12345}");

        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.get(anyString(), anyString(), any(HttpClient.class)))
                       .thenReturn(response);

            GitHubAppOrg org = gitHubApp.org("my-org");

        }
    }

    @Test
    void testOrgNotFound() throws Exception {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(404);

        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.get(anyString(), anyString(), any(HttpClient.class)))
                       .thenReturn(response);

            assertThrows(GitHubAppException.class, () -> gitHubApp.org("non-existent-org"));
        }
    }

    @Test
    void testPostWithJsonObject() throws Exception {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.body()).thenReturn("{\"success\": true}");

        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.post(anyString(), anyString(), anyString(), any(HttpClient.class)))
                       .thenReturn(response);

            JSONObject jsonBody = new JSONObject().put("key", "value");
            HttpResponse<String> result = gitHubApp.post("/some/url", jsonBody);

            assertEquals("{\"success\": true}", result.body());
        }
    }

    @Test
    void testPostWithString() throws Exception {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.body()).thenReturn("{\"success\": true}");

        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.post(anyString(), anyString(), anyString(), any(HttpClient.class)))
                       .thenReturn(response);

            String body = "{\"key\":\"value\"}";
            HttpResponse<String> result = gitHubApp.post("/some/url", body);

            assertEquals("{\"success\": true}", result.body());
        }
    }
}
