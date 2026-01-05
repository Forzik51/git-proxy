package com.githubrestapi;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/github")
public class RepositoriesController {
    private final RepositoriesService repositoriesService;

    public RepositoriesController(RepositoriesService repositoriesService) {
        this.repositoriesService = repositoriesService;
    }

    @GetMapping("/users/{username}/repositories")
    public List<RepositoryResponse> listNonForkRepositories(@PathVariable String username) {
        return repositoriesService.listNonForkRepositories(username);
    }
}
