````md
# GitHub REST API Proxy (Non-fork Repositories + Branches)

A small Spring Boot 4 (Spring MVC) application that acts as a proxy to the GitHub REST API v3.  
It exposes a single endpoint that returns **all repositories of a given GitHub user that are not forks**, including repository owner login and branch details (branch name + last commit SHA).
## Tech stack

* Java 25
* Spring Boot 4.0.1
* Gradle (Kotlin DSL)
* Runtime dependencies:

  * `org.springframework.boot:spring-boot-starter-webmvc`
  * `org.springframework.boot:spring-boot-starter-restclient`
* Testing:

  * Spring Boot test support
  * WireMock (HTTP server for integration tests)

## Configuration

The application uses a single property:

* `github.base-url` – GitHub API base URL

Default value in `src/main/resources/application.properties`:

```properties
github.base-url=https://api.github.com
```


## Build & run

### Run tests

```bash
./gradlew test
```

### Run the application locally

```bash
./gradlew bootRun
```

App starts on `http://localhost:8080`.

## API

### List non-fork repositories for a user

**GET**

```
/github/users/{username}/repositories
```

Example:

```bash
curl -s http://localhost:8080/github/users/Forzik51/repositories
```

Example response:

```json
[
  {
    "repositoryName": "Hello-World",
    "ownerLogin": "Forzik51",
    "branches": [
      {
        "name": "master",
        "lastCommitSha": "7fd1a60b01f91b314f59955a4e4d..."
      }
    ]
  }
]
```

### User not found

If the GitHub user does not exist, the API returns **404**:

```json
{
  "status": 404,
  "message": "GitHub user 'some-user' not found"
}
```

## Backing GitHub API (v3)

The app uses GitHub REST API v3 endpoints:

* List repositories:

  * `GET /users/{username}/repos`
* List branches:

  * `GET /repos/{owner}/{repo}/branches`  