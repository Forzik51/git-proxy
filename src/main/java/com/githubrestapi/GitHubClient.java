package com.githubrestapi;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

@Component
public class GitHubClient {
    private final RestClient restClient;

    GitHubClient(RestClient restClient) {
        this.restClient = restClient;
    }

    List<GitHubRepo> listUserRepositories(String username){
        try{
            var body = restClient.get()
                    .uri("/users/{username}/repos",username)
                    .retrieve()
                    .body(GitHubRepo[].class);

            return body == null ? List.of() : Arrays.asList(body);

        } catch (HttpClientErrorException ex) {
            if(ex.getStatusCode() == HttpStatus.NOT_FOUND){
                throw new GitHubUserNotFoundException(username);
            }
            throw ex;
        }
    }

    List<GitHubBranch> listBranches(String owner, String repo){
        var body = restClient.get()
                .uri("/repos/{owner}/{repo}/branches",owner,repo)
                .retrieve()
                .body(GitHubBranch[].class);

        return body == null ? List.of() : Arrays.asList(body);
    }
}
