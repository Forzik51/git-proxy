package com.githubrestapi;

public record GitHubRepo (
        String name,
        boolean fork,
        GitHubOwner owner
) { }
