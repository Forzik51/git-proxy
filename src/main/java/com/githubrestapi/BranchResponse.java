package com.githubrestapi;

public record BranchResponse (
        String name,
        String lastCommitSha
){ }
