package com.githubrestapi;

public record GitHubBranch (
        String name,
        GitHubCommit commit
){ }
