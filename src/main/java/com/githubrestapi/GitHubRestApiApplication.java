package com.githubrestapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@SpringBootApplication
public class GitHubRestApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(GitHubRestApiApplication.class, args);
    }

    @Bean
    RestClient gitHubRestClient(RestClient.Builder builder, @Value("${github.base-url}") String baseUrl) {
        return builder.baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.USER_AGENT, "github-restapi")
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .build();
    }

}
