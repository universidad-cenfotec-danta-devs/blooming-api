This repository contains a Spring Boot application that uses Maven as the build tool. Follow the steps below to set up and run the application locally.

## Prerequisites

Make sure you have the following installed on your machine:
- **Java 11 or higher**
- **Maven**
- **MariaDB**

## Database Setup

1. **MariaDB Database**: The application requires a MariaDB database set up locally. Create a database and ensure you have the necessary credentials.
2. **Configure Connection Values in `application.properties`**: In the `src/main/resources/application.properties` file, replace the following values with your environment-specific information:

```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/INSERT_DB
spring.datasource.username=INSERT_USER
spring.datasource.password=INSERT_PASSWORD
```


# Running the Application

In the project root, run the following command to compile and build the application:

```bash
mvn clean install
```

Skip test (IF necessary)
```bash
mvn clean install -DskipTests
```

After building, you can run the application with:
```bash
mvn spring-boot:run
```