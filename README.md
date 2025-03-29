# Release Guard ⛨

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.25-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen.svg)](https://spring.io/projects/spring-boot)

Release Guard is a Spring Boot application that helps validate Jira issue readiness for releases by checking blocking status and providing simplified issue information.

## Key Features ✨

- Retrieve simplified Jira issue information
- Check if an issue is blocked by unresolved dependencies
- REST API for integration with CI/CD pipelines
- Custom exception handling with proper HTTP status codes
- Comprehensive test coverage (unit & integration tests)

## Technologies Used 🛠️

- **Kotlin** 1.9.25
- **Spring Boot** 3.4.4
- **Gradle** 8.13
- **JUnit 5** & **MockK** for testing
- **Jackson** for JSON processing

## Installation & Setup 🚀

### Prerequisites
- Java 21 JDK
- Gradle 8.x
- Valid Jira account with API access

### Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/release-guard.git
   cd release-guard
   ```

2. Configure Jira credentials in src/main/resources/application.properties:
    ```properties
    jira.instance-url=https://your-jira-instance.atlassian.net
    jira.email=your@email.com
    jira.api-token=your-api-token
    ```

3. Build the application:
    ```bash
    ./gradlew build
    ```

4. Run the application:
    ```bash
    ./gradlew bootRun
    ```

## API Documentation 📚
### Get Simplified Issue Information

**Request:**
```http
GET /api/jira/issue/{issueKey}
```

**Response:**

```json
{
"key": "JIRA-123",
"summary": "Implement security layer",
"status": "IN_PROGRESS",
"linkedIssues": [
    {
    "key": "JIRA-456",
    "type": "BLOCKS",
    "status": "TO_DO",
    "linkDirection": "INWARD"
    }
]
}
```

### Check Block Status

**Request:**

```http
GET /api/jira/issue/{issueKey}/block-status
```

**Response:**

`true` if unblocked, `false` if blocked

## Testing 🧪
**Run all tests:**

```bash
./gradlew test
```

**Test coverage includes:**
- Jira client integration tests
- Controller layer tests
- Service logic tests
- Exception handling tests
- Model assembler tests

## Contributing 🤝
**We welcome contributions! Please follow these steps:**
1. Fork the repository
2. Create a feature branch (git checkout -b feature/amazing-feature)
3. Commit your changes (git commit -m 'Add amazing feature')
4. Push to the branch (git push origin feature/amazing-feature)
5. Open a Pull Request

> 💡 **Please ensure all tests pass and include new tests for any added functionality.**

## Configuration ⚙️
**Environment Variables:**

Property | Description
------------|-----------
jira.instance-url | Base URL of your Jira instance
jira.email	Email | for Jira authentication
jira.api-token | Jira API token

## License 📄
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
