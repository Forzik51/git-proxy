package com.githubrestapi;

public class GitHubUserNotFoundException extends RuntimeException{
    public GitHubUserNotFoundException(String username){
        super("GitHub user '%s' not found".formatted(username));
    }
}
