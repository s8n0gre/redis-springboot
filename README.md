# Shopping Cart Session Management Assignment

## Architecture Diagram

```
                +-------------------+
                |   Client (Postman |
                |   /Browser)       |
                +---------+---------+
                          |
                          v
                +---------+---------+
                |   Controller      |
                | (UserController)  |
                +---------+---------+
                          |
          +---------------+---------------+
          |                               |
          v                               v
 +---------------------+        +---------------------+
 |  Redis Repository   |        |  Oracle Repository  |
 | (UserRepository)    |        | (UserJdbcRepository)|
 +---------+-----------+        +----------+----------+
           |                               |
           v                               v
   +-------+-------+               +-------+-------+
   |   Redis DB    |               |  Oracle DB   |
   +---------------+               +---------------+

   (Session, cache,                (Persistent
    atomic counters,                user/session
    leaderboard, etc.)              data)

```

**Model Layer** (User.java) is used throughout to transfer data between all layers.

## What is Redis?

**Redis** (Remote Dictionary Server) is an open-source, in-memory data structure store, used as a database, cache, and message broker. It supports data structures such as strings, hashes, lists, sets, sorted sets, bitmaps, and more. Redis is known for its high performance, low latency, and support for advanced features.

### Common Use Cases for Redis
- **Caching**: Store frequently accessed data in memory to reduce database load and improve response times.
- **Share User Session Data**: Store session information (like shopping cart contents) so it can be accessed by any server in a distributed system.
- **Distributed Data**: Redis can be used as a distributed key-value store, supporting replication and clustering for high availability and scalability.
- **Atomic Counters**: Redis provides atomic increment/decrement operations, useful for counting page views, likes, or inventory.
- **Leaderboard**: Use Redis sorted sets to efficiently implement leaderboards (e.g., for games or sales rankings).

In this project, Redis is used for session management, but it can also be extended for caching, distributed counters, and more advanced features as needed in large-scale applications.

## Brief: Stateful vs Stateless Architecture

### Flipkart Example: Stateful vs Stateless HTTP

**Stateful Approach:**
- In a traditional stateful web application, Flipkart’s servers would keep all user session data (cart contents, login state, preferences) in memory for each user.
- If a user’s requests are always routed to the same server, their session is preserved. But if the server crashes or the user is routed to a different server, their session is lost.
- Scaling is difficult: you need sticky sessions (load balancer affinity) or session replication, which adds complexity and overhead.

**Stateless Approach (as in this project):**
- Each HTTP request from the Flipkart client (browser/app) includes a session ID (in a cookie or header).
- Any server can handle any request, because session data is stored centrally in Redis (not in server memory).
- If a server crashes or the user is routed to a new server, their session is still available—no data loss, no sticky sessions needed.
- This enables easy scaling, high availability, and seamless failover, which is essential for large-scale e-commerce sites like Flipkart.

**Summary:**
- Stateless HTTP with Redis enables Flipkart to provide a seamless, reliable shopping experience at scale, while stateful architectures would struggle with reliability and scalability in such a distributed environment.

**Stateful Architecture:**
In a stateful system, the server maintains session information about each client across multiple requests. This means the server remembers previous interactions, making it easier to manage user sessions, but it can limit scalability and fault tolerance.

**Stateless Architecture:**
In a stateless system, each HTTP request from a client contains all the information needed for the server to fulfill that request. The server does not retain any session information between requests. This approach is highly scalable and fault-tolerant, but requires external mechanisms (like Redis) to manage session data for applications that need to maintain user state (such as shopping carts).

**This Project:**
Implements session management for a stateful shopping cart using stateless HTTP by storing session data in Redis, allowing the application to scale while maintaining user session consistency.

## Architecture Overview

This project uses the Model-View-Controller (MVC) pattern with Spring Boot:
- **Controller Layer**: Handles HTTP requests and responses (see `UserController.java`).
- **Service/Repository Layer**: Contains business logic and data access (see `UserRepository.java` for Redis, `UserJdbcRepository.java` for Oracle DB).
- **Model Layer**: Represents data objects (see `User.java`).

### How MVC, Redis, and SQL DB Work Together
- The **Controller** receives API calls (e.g., add to cart, get user info).
- The **Repository** layer stores/retrieves data from Redis (for fast access/session) and Oracle (for persistence).
- The **Model** is used to transfer data between layers and as the structure for Redis/DB storage.

#### Example Code Locations to Explain in Your Assignment
- `UserController.java`: Show how endpoints map to controller methods and how requests are routed.
- `UserRepository.java`: Show how Redis is used for fast, in-memory session storage.
- `UserJdbcRepository.java`: Show how Oracle SQL is used for persistent storage and upsert logic.
- `User.java`: Show how the model is used for both Redis and Oracle DB.
- `RedisConfig.java`: Show how RedisTemplate is configured for object and string operations.

You can add code comments in these files to explain:
- Why Redis is used for session/state (speed, scalability)
- Why Oracle is used for persistence (durability, reporting)
- How the controller coordinates both stores for consistency


## Project Overview
This project demonstrates how to implement session management for a stateful shopping cart application (like Flipkart) using stateless HTTP, with a focus on:
- Using Redis for fast, scalable session storage
- Using Oracle SQL for persistent user data
- Exposing RESTful endpoints for session and user management

## Steps and Features Implemented

### Detailed Flow Example
1. **User adds item to cart (POST /users/set)**
   - Controller receives request, stores session data in Redis, and persists to Oracle.
2. **User session is retrieved (GET /users/{id})**
   - Controller fetches user/session data from Redis for fast access.
3. **Session consistency**
   - Both Redis and Oracle are updated to ensure no data loss.


1. **Spring Boot Project Setup**
   - Created a Spring Boot project with REST endpoints for user/session management.
   - Configured Maven dependencies for Spring Web, Spring Data Redis, Spring JDBC, and Oracle JDBC driver.

2. **Redis Integration**
   - Configured Redis as a session store using Docker (`my-redis` container).
   - Implemented endpoints to store and retrieve session/user data in Redis, both as simple key-value pairs and as structured objects.
   - Used `RedisTemplate` for object storage and `StringRedisTemplate` for string operations.

3. **Oracle SQL Integration**
   - Configured Oracle SQLPlus as the persistent database using JDBC.
   - Created a `users` table for storing user data.
   - Implemented upsert logic: if a user already exists, their data is updated instead of causing a unique constraint error.

4. **REST Endpoints**
   - `/users` (POST): Stores a user object in both Redis and Oracle.
   - `/users/set` (POST): Stores a key-value pair in Redis and as a user in Oracle, also as a structured object in Redis.
   - `/users/get` (GET): Retrieves a value from Redis.
   - `/users/{id}` (GET/DELETE): Retrieves or deletes a user object from Redis.

5. **Session Management Approach**
   - Used Redis to store session data (cart contents, user info) with a unique session ID (could be stored in a cookie or header).
   - Stateless HTTP: Each request includes the session ID, allowing the server to fetch session state from Redis.
   - Ensured data consistency by updating both Redis (for fast access) and Oracle (for persistence).

6. **Security Considerations & Mitigations**
   - **Session Hijacking**: Use secure, random session IDs; transmit over HTTPS; set HttpOnly and Secure flags on cookies.
   - **Session Fixation**: Regenerate session IDs after login.
   - **Data Tampering**: Never trust client-side data; always validate session IDs and user input.
   - **Redis Exposure**: Protect Redis with authentication, firewall, and network isolation.
   - **SQL Injection**: Used parameterized queries with JDBC to prevent injection attacks.

## Technologies Used
- Java 17
- Spring Boot
- Spring Data Redis
- Spring JDBC
- Oracle SQL (SQLPlus)
- Redis (Docker)
- Postman (for API testing)

## How This Meets the Assignment

## Suggestions for Your Assignment Writeup
- Add code snippets from the files above to illustrate each layer (Controller, Repository, Model).
- Explain the flow of a request from HTTP → Controller → Redis/Oracle → Response.
- Discuss why stateless HTTP needs a session store (Redis) and how you keep it consistent with the DB.
- Highlight security and scalability benefits of this approach.

- Demonstrates stateless HTTP session management using Redis.
- Ensures data consistency between fast-access (Redis) and persistent (Oracle) storage.
- Addresses security vulnerabilities and provides mitigations.
- Provides a working codebase with clear, testable endpoints.

---

## Security Issues and Mitigations in Production

### Common Security Issues with Redis and Stateless Architectures
- **Session Hijacking**: Attackers may steal session IDs from cookies or URLs and impersonate users.
- **Session Fixation**: Attackers force a user to use a known session ID, then hijack the session after login.
- **Data Tampering**: Without proper validation, attackers may manipulate session data or inject malicious payloads.
- **Replay Attacks**: Reusing intercepted requests to perform unauthorized actions.
- **Redis Exposure**: If Redis is not properly secured, attackers can access or modify all session data.
- **Distributed Denial of Service (DDoS)**: Stateless APIs are easier to scale, but also easier to target with high-volume attacks.

### Mitigation Strategies
- **Spring Security**: Use Spring Security to manage authentication, authorization, and session management. It provides built-in protections against session fixation, CSRF, and more.
- **HTTPS Everywhere**: Always use HTTPS to encrypt traffic and protect session IDs in transit.
- **HttpOnly and Secure Cookies**: Store session IDs in cookies with `HttpOnly` and `Secure` flags to prevent access from JavaScript and ensure transmission only over HTTPS.
- **Session Expiry and Rotation**: Set short session timeouts and rotate session IDs after login or privilege changes.
- **Input Validation and Output Encoding**: Prevent injection attacks by validating all user input and encoding output.
- **Redis Security**: Enable Redis authentication, use firewalls, and restrict network access. Consider Redis ACLs and TLS for encryption.
- **Rate Limiting and DDoS Protection**: Use API gateways, rate limiting, and WAFs (Web Application Firewalls) to protect against abuse.
- **Monitoring and Logging**: Monitor for suspicious activity and log all authentication/session events for auditing.

By combining these best practices and technologies, production systems can significantly reduce the risk of session-related vulnerabilities and ensure secure, scalable session management.

---

## How to Switch Servers (Redis/Oracle/Environment)

To switch between different Redis or Oracle servers (for example, moving from development to production, or changing cloud providers):

1. **Update Configuration:**
   - Edit `src/main/resources/application.properties`.
   - Change the following properties to point to your new server:
     - For Redis:
       - `spring.data.redis.host=NEW_REDIS_HOST`
       - `spring.data.redis.port=NEW_REDIS_PORT`
     - For Oracle:
       - `spring.datasource.url=jdbc:oracle:thin:@NEW_HOST:PORT/SERVICE`
       - `spring.datasource.username=NEW_USERNAME`
       - `spring.datasource.password=NEW_PASSWORD`

2. **Restart the Application:**
   - After saving the changes, restart your Spring Boot application to connect to the new servers.

3. **(Optional) Use Environment Variables:**
   - For production, you can use environment variables or externalized configuration to avoid hardcoding secrets in your codebase.
   - Example (in `application.properties`):
     - `spring.data.redis.host=${REDIS_HOST}`
     - `spring.datasource.password=${ORACLE_PASSWORD}`

4. **Cloud/Container Deployments:**
   - If deploying with Docker, Kubernetes, or cloud platforms, set these values as environment variables or secrets in your deployment configuration.

This makes it easy to switch between servers or environments without changing your application code.

---

## How to Bounce Between Stateless Servers

In a stateless architecture, you can easily move ("bounce") between servers for scaling, failover, or maintenance. Here’s how you can do it:

### Using SSH
- SSH into any server running your application:
  ```sh
  ssh user@your-server-ip
  cd /path/to/your/app
  ./mvnw spring-boot:run
  # or
  docker-compose up -d
  ```
- Because the app is stateless, you can start/stop it on any server. All session data is in Redis/Oracle, not local memory.

### Using Docker
- Build and run your app container on any host:
  ```sh
  docker build -t myapp .
  docker run -d --env-file .env -p 8080:8080 myapp
  ```
- You can stop a container on one host and start it on another with the same environment variables.

### Using Kubernetes
- Deploy your app as a Deployment or StatefulSet:
  ```yaml
  apiVersion: apps/v1
  kind: Deployment
  metadata:
    name: myapp
  spec:
    replicas: 3
    template:
      spec:
        containers:
        - name: myapp
          image: myapp:latest
          envFrom:
          - configMapRef:
              name: myapp-config
  ```
- Kubernetes will automatically bounce (reschedule) pods across nodes as needed. All pods connect to the same Redis/Oracle backend.

**Key Point:**
Because the application is stateless, you can scale up/down, restart, or move instances at any time without losing user session data. All state is managed in Redis and Oracle.

Feel free to expand on any section for your assignment or ask for more details/examples!
