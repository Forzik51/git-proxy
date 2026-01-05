package com.githubrestapi;

import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepositoriesService {

    private final GitHubClient gitHubClient;

    RepositoriesService(GitHubClient gitHubClient) {
        this.gitHubClient = gitHubClient;
    }

    List<RepositoryResponse> listNonForkRepositories(String username) {
        return gitHubClient.listUserRepositories(username).stream()
                .filter(repo -> !repo.fork())
                .map(repo -> {
                    var branches = gitHubClient.listBranches(repo.owner().login(), repo.name()).stream()
                            .map(b -> new BranchResponse(b.name(), b.commit().sha()))
                            .toList();
                    return new RepositoryResponse(repo.name(), repo.owner().login(), branches);
                })
                .toList();
    }
}
