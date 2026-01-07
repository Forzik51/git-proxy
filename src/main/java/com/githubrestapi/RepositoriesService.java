package com.githubrestapi;

import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class RepositoriesService {

    private final GitHubClient gitHubClient;
    private final ExecutorService executorService;

    RepositoriesService(GitHubClient gitHubClient, ExecutorService executorService) {
        this.gitHubClient = gitHubClient;
        this.executorService = executorService;
    }

    List<RepositoryResponse> listNonForkRepositories(String username) {

            var futures = gitHubClient.listUserRepositories(username).stream()
                    .filter(repo -> !repo.fork())
                    .map(repo -> CompletableFuture.supplyAsync(() -> {
                var branches = gitHubClient.listBranches(repo.owner().login(), repo.name()).stream()
                        .map(b -> new BranchResponse(b.name(), b.commit().sha()))
                        .toList();
                return new RepositoryResponse(repo.name(), repo.owner().login(), branches);
            }, executorService))
                    .toList();

            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
            return futures.stream().map(CompletableFuture::join).toList();



    }
}
