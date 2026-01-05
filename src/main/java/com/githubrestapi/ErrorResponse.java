package com.githubrestapi;

public record ErrorResponse (
        int status,
        String message
){ }
