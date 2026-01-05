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
    }

    @Test
    void shouldReturnOnlyNonForkReposWithBranchesAndLastCommitSha() {
        wireMockServer.stubFor(get(urlPathEqualTo("/users/TheSoftwareHouse/repos"))
                .willReturn(okJson("""
                        [
                          {"name":"API-Platform-webinar","fork":false,"owner":{"login":"TheSoftwareHouse"}},
                          {"name":"docker-openvpn","fork":true,"owner":{"login":"TheSoftwareHouse"}}
                        ]
                        """)));

        wireMockServer.stubFor(get(urlPathEqualTo("/repos/TheSoftwareHouse/API-Platform-webinar/branches"))
                .willReturn(okJson("""
                        [
                          {"name":"master","commit":{"sha":"b6370f3e881a543ed0c283ae3e13aeb59f3e279d"}}
                        ]
                        """)));

        ResponseEntity<RepositoryResponse[]> response =
                http.getForEntity("/github/users/TheSoftwareHouse/repositories", RepositoryResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        var repos = response.getBody();
        assertThat(repos).hasSize(1);

        assertThat(repos[0].repositoryName()).isEqualTo("API-Platform-webinar");
        assertThat(repos[0].ownerLogin()).isEqualTo("TheSoftwareHouse");
        assertThat(repos[0].branches()).hasSize(1);

        assertThat(repos[0].branches().get(0).name()).isIn("master");
        assertThat(repos[0].branches().stream().map(BranchResponse::lastCommitSha))
                .containsExactlyInAnyOrder("b6370f3e881a543ed0c283ae3e13aeb59f3e279d");
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
