# Fund Transfer Project

## Aim
The aim of this project is to develop a REST API to transfer money from one account to another.

## Main Project Features
- Performs currency exchange if the source and destination currencies differ.
- Handles concurrency to ensure data integrity during simultaneous transactions.
- Keeps track of failed transactions, such as when an account has insufficient balance.

## Business Assumptions
- The amount and currency in the request are validated by a hypothetical front end.
- The sender is aware of the exchange rate before initiating the transfer.
- The specific response message is handled by the hypothetical front end; the back end returns a transaction ID if successful (HTTP 2xx) or a Problem object in case of an error.

## Next Steps
- Implement a caching mechanism or dedicated domain for exchange rates.
- Add PostgreSQL DB.
- Introduce coherent entities (e.g., User).
- Develop an admin API.

## Tech Stack
- Java 11
- Spring Boot
- Spring Boot WebFlux
- Spring Boot Test
- Hibernate/JPA
- H2 Database

## Methodology
- Domain-Driven Design (DDD)

## Run Project
1. Clone the repository:
    ```sh
    git clone <repository-url>
    ```
2. Navigate to the project directory:
    ```sh
    cd fund-transfer
    ```
3. Build the project:
    ```sh
    mvn clean install
    ```
4. Run the application:
    ```sh
    mvn spring-boot:run
    ```

This README file provides a comprehensive overview of the Fund Transfer project, covering its aim, features, business assumptions, next steps, tech stack, methodology, and instructions for running the project.