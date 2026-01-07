package com.githubrestapi;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.*;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.StopWatch;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
public class RepositoriesControllerIT {
    private static final WireMockServer wireMockServer =
            new WireMockServer(WireMockConfiguration.options().dynamicPort());

    @Autowired
    TestRestTemplate http;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {

        registry.add("github.base-url", wireMockServer::baseUrl);
        registry.add("spring.mvc.servlet.load-on-startup", () -> "1");
    }

    @BeforeAll
    static void beforeAll() {
        wireMockServer.start();
    }

    @AfterAll
    static void afterAll() {
        wireMockServer.stop();
    }

    @BeforeEach
    void beforeEach() {
        wireMockServer.resetAll();
        configureFor("localhost", wireMockServer.port());
    }

    @Test
    void shouldReturnOnlyNonForkReposWithBranchesAndLastCommitSha() {
        wireMockServer.stubFor(get(urlPathEqualTo("/users/Forzik51/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withFixedDelay(1000)
                        .withBody(
                        """
                        [
                            {"name":"repo-a","fork":false,"owner":{"login":"Forzik51"}},
                            {"name":"repo-b","fork":false,"owner":{"login":"Forzik51"}},
                            {"name":"repo-fork","fork":true,"owner":{"login":"Forzik51"}}
                        ]
                        """)));

        wireMockServer.stubFor(get(urlPathEqualTo("/repos/Forzik51/repo-a/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withFixedDelay(1000)
                        .withBody(
                        """
                        [
                            {"name":"master","commit":{"sha":"sha-a-1"}},
                            {"name":"develop","commit":{"sha":"sha-a-2"}},
                            {"name":"release","commit":{"sha":"sha-a-3"}}
                        ]
                        """)));

        wireMockServer.stubFor(get(urlPathEqualTo("/repos/Forzik51/repo-b/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withFixedDelay(1000)
                        .withBody(
                                """
                                [
                                    {"name":"master","commit":{"sha":"sha-b-1"}},
                                    {"name":"develop","commit":{"sha":"sha-b-2"}},
                                    {"name":"release","commit":{"sha":"sha-b-3"}}
                                ]
                                """)));

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        ResponseEntity<RepositoryResponse[]> response =
                http.getForEntity("/github/users/Forzik51/repositories", RepositoryResponse[].class);

        stopWatch.stop();
        long elapsedTime = stopWatch.getTotalTimeMillis();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        var repos = response.getBody();
        assertThat(repos).hasSize(2);

        assertThat(repos[0].repositoryName()).isEqualTo("repo-a");
        assertThat(repos[0].ownerLogin()).isEqualTo("Forzik51");
        assertThat(repos[0].branches()).hasSize(3);
        assertThat(repos[1].branches()).hasSize(3);

        assertThat(repos[0].branches().get(0).name()).isIn("master");

        verify(3, getRequestedFor(urlMatching(".*")));

        System.out.println("time: "+elapsedTime);

        assertThat(elapsedTime)
                .isGreaterThan(2000L)
                .isLessThan(3000L);
    }

    @Test
    void shouldReturn404WithExpectedErrorBodyWhenUserDoesNotExist() {
        wireMockServer.stubFor(get(urlPathEqualTo("/users/ghost/repos"))
                .willReturn(aResponse().withStatus(404)));

        ResponseEntity<ErrorResponse> response =
                http.getForEntity("/github/users/ghost/repositories", ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().message()).contains("ghost").contains("not found");
    }
}
