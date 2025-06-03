# Track4j 🚀

[![Java Version](https://img.shields.io/badge/Java-11%2B-blue.svg)](https://openjdk.java.net/projects/jdk/11/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)]()
[![Development Status](https://img.shields.io/badge/Status-In%20Development-orange.svg)]()

**Track4j** is a lightweight, high-performance Java library for comprehensive business process journey tracking. It automatically captures and logs all HTTP requests, external API calls, and internal method invocations with distributed tracing capabilities.

⚠️ **Development Status**: This library is currently under active development and should not be used in production environments.

## ✨ Features

- 🔍 **Complete Journey Tracking**: Monitor incoming requests, external API calls, and internal method executions
- 🌐 **Distributed Tracing**: Built-in trace ID and span ID generation for request correlation
- 📊 **Multiple Storage Options**: Support for PostgreSQL, SQLite, and extensible storage backends
- ⚡ **High Performance**: Asynchronous batch processing with configurable flush intervals
- 🎯 **Annotation-Based**: Simple `@Track4j` annotation for selective tracking
- 🔧 **Highly Configurable**: Extensive configuration options for fine-tuned control
- 📝 **Rich Metadata**: Capture headers, request/response bodies, timing, and custom tags
- 🚫 **Smart Filtering**: Exclude unwanted endpoints with pattern-based filtering
- 💾 **Memory Efficient**: Configurable body size limits and batch processing

## 🎯 Use Cases

- **API Monitoring**: Track all incoming and outgoing HTTP requests
- **Performance Analysis**: Measure request duration and identify bottlenecks
- **Debugging**: Trace request flows across multiple services
- **Audit Logging**: Maintain compliance with detailed request/response logs
- **Business Intelligence**: Analyze user journeys and API usage patterns

## 📋 Requirements

- **Java**: 11 or higher
- **Database**: PostgreSQL or SQLite
- **Spring Boot**: 2.x or 3.x (optional but recommended)

## 🚀 Quick Start

### 1. Add Dependency

#### Maven
```xml
<dependency>
    <groupId>io.track4j</groupId>
    <artifactId>track4j</artifactId>
    <version>0.1.0</version>
</dependency>
```

#### Gradle
```groovy
implementation 'io.track4j:track4j:0.1.0'
```

### 2. Configuration

Create a `track4j.properties` file in your `src/main/resources`:

```properties
# Core Configuration
track4j.enabled=true
track4j.storage-type=SQL

# Database Configuration
track4j.connection-url=jdbc:sqlite:track4j_logs.db
track4j.driver-class-name=org.sqlite.JDBC
track4j.connection-username=user
track4j.connection-password=pass

# Tracking Options
track4j.incoming-request-tracking-enabled=true
track4j.external-request-tracking-enabled=true
track4j.internal-call-tracking-enabled=true

# Performance Settings
track4j.batch-size=1000
track4j.flush-interval=15000
track4j.filter-order=-100

# Content Settings
track4j.include-request-body=true
track4j.include-response-body=true
track4j.include-headers=true

# Filtering
track4j.exclude-patterns=/actuator/**,/health/**,/swagger-ui/**

# Hikari
track4j.hikari.maximum-pool-size=10
track4j.hikari.minimum-idle=5
track4j.hikari.connection-timeout=30000
track4j.hikari.idle-timeout=600000
track4j.hikari.max-lifetime=1800000
track4j.hikari.auto-commit=false
track4j.hikari.pool-name=track4j-pool
```

### 3. Database Setup

#### SQLite (Default)
```sql
CREATE TABLE request_logs (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    trace_id VARCHAR(48) NOT NULL,
    span_id VARCHAR(11),
    parent_span_id VARCHAR(11),
    service_name VARCHAR(100),
    operation_name VARCHAR(2055),
    request_type VARCHAR(8) CHECK (request_type IN ('INCOMING', 'EXTERNAL', 'INTERNAL')),
    method VARCHAR(6),
    url VARCHAR(2048),
    request_headers TEXT,
    request_body TEXT,
    response_headers TEXT,
    response_body TEXT,
    status_code INTEGER,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    duration_ms INTEGER,
    success BOOLEAN,
    error_message TEXT,
    user_id VARCHAR(100),
    client_ip VARCHAR(45),
    tags TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### PostgreSQL
```sql
CREATE TABLE request_logs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  trace_id VARCHAR(48) NOT NULL,
  span_id VARCHAR(11),
  parent_span_id VARCHAR(11),
  service_name VARCHAR(100),
  operation_name VARCHAR(2055),
  request_type VARCHAR(8) CHECK (request_type IN ('INCOMING', 'EXTERNAL', 'INTERNAL')),
  method VARCHAR(6),
  url VARCHAR(2048),
  request_headers TEXT,
  request_body TEXT,
  response_headers TEXT,
  response_body TEXT,
  status_code INTEGER,
  start_time TIMESTAMP WITH TIME ZONE,
  end_time TIMESTAMP WITH TIME ZONE,
  duration_ms INTEGER,
  success BOOLEAN,
  error_message TEXT,
  user_id VARCHAR(100),
  client_ip VARCHAR(39),
  tags TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

```

### 4. Usage Examples

#### Basic Controller Tracking
```java
@RestController
@RequestMapping("/api")
@Track4j(name = "UserController", tags = {"user", "api"})
public class UserController {
    
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        // This method will be automatically tracked
        return ResponseEntity.ok(userService.findById(id));
    }
}
```

#### Service Method Tracking
```java
@Service
public class ProductService {
    
    @Track4j(name = "saveProduct", tags = {"product", "database"})
    public Product saveProduct(Product product) {
        // Internal method tracking
        return productRepository.save(product);
    }
}
```

#### External API Calls (Automatic)
```java
@Component
public class ExternalApiClient {
    
    private final RestTemplate restTemplate;
    
    public TodoResponse getTodo(Long id) {
        // External calls are automatically tracked when enabled
        return restTemplate.getForObject(
            "https://jsonplaceholder.typicode.com/todos/" + id, 
            TodoResponse.class
        );
    }
}
```

## 📖 Configuration Reference

### Core Settings

| Property | Default | Description |
|----------|---------|-------------|
| `track4j.enabled` | `true` | Enable/disable Track4j globally |
| `track4j.storage-type` | `SQL` | Storage backend type |
| `track4j.service-name` | Auto-detected | Service name for tracing |

### Database Settings

| Property | Required | Description |
|----------|----------|-------------|
| `track4j.connection-url` | ✅ | JDBC connection URL |
| `track4j.driver-class-name` | ✅ | JDBC driver class |
| `track4j.username` | ❌ | Database username |
| `track4j.password` | ❌ | Database password |

### Tracking Settings

| Property | Default | Description |
|----------|---------|-------------|
| `track4j.incoming-request-tracking-enabled` | `true` | Track incoming HTTP requests |
| `track4j.external-request-tracking-enabled` | `true` | Track outgoing HTTP requests |
| `track4j.internal-call-tracking-enabled` | `true` | Track internal method calls |

### Performance Settings

| Property | Default | Description |
|----------|---------|-------------|
| `track4j.batch-size` | `1000` | Number of records to batch before flush |
| `track4j.flush-interval` | `15000` | Flush interval in milliseconds |
| `track4j.filter-order` | `-100` | Filter execution order |

### Content Settings

| Property | Default | Description |
|----------|---------|-------------|
| `track4j.include-request-body` | `true` | Include request body in logs |
| `track4j.include-response-body` | `true` | Include response body in logs |
| `track4j.include-headers` | `true` | Include HTTP headers in logs |

### Filtering Settings

| Property | Default | Description |
|----------|---------|-------------|
| `track4j.exclude-patterns` | Empty | Comma-separated URL patterns to exclude |

### HikariCP Connection Pool Settings

| Property | Default | Description |
|----------|---------|-------------|
| `track4j.hikari.maximum-pool-size` | `10` | Maximum number of connections in the pool |
| `track4j.hikari.minimum-idle` | `5` | Minimum number of idle connections to maintain |
| `track4j.hikari.connection-timeout` | `30000` | Maximum time (ms) to wait for a connection from pool |
| `track4j.hikari.idle-timeout` | `600000` | Maximum time (ms) a connection can sit idle (10 minutes) |
| `track4j.hikari.max-lifetime` | `1800000` | Maximum lifetime (ms) of a connection in pool (30 minutes) |
| `track4j.hikari.auto-commit` | `false` | Enable/disable auto-commit for connections |
| `track4j.hikari.pool-name` | `track4j-pool` | Name identifier for the connection pool |

## 🏷️ Annotation Reference

### @Track4j

```java
@Track4j(
    name = "operationName",          // Operation name (default: method name)
    tags = {"tag1", "tag2"},         // Custom tags for categorization
    includeArgs = true,              // Include method args
    includeResult = true,            // Include result
    enabled = true                   // Enable/Disable annotation
)
```

## 📊 Data Model

### Request Log Fields

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID/VARCHAR | Unique identifier |
| `trace_id` | VARCHAR(32) | Distributed trace ID |
| `span_id` | VARCHAR(32) | Current span ID |
| `parent_span_id` | VARCHAR(32) | Parent span ID |
| `service_name` | VARCHAR(100) | Service name |
| `operation_name` | VARCHAR(200) | Operation/method name |
| `request_type` | ENUM | `INCOMING`, `EXTERNAL`, `INTERNAL` |
| `method` | VARCHAR(10) | HTTP method |
| `url` | VARCHAR(1000) | Request URL |
| `request_headers` | TEXT | Request headers |
| `request_body` | TEXT | Request body |
| `response_headers` | TEXT | Response headers |
| `response_body` | TEXT | Response body |
| `status_code` | INTEGER | HTTP status code |
| `start_time` | TIMESTAMP | Request start time |
| `end_time` | TIMESTAMP | Request end time |
| `duration_ms` | INTEGER | Duration in milliseconds |
| `success` | BOOLEAN | Success flag |
| `error_message` | TEXT | Error message if failed |
| `user_id` | VARCHAR(100) | User identifier |
| `client_ip` | VARCHAR(45) | Client IP address |
| `tags` | TEXT | Custom tags |
| `created_at` | TIMESTAMP | Record creation time |

## 📈 Monitoring and Analytics

### Query Examples

#### Get Request Journey
```sql
SELECT * FROM request_logs 
WHERE trace_id = 'your-trace-id' 
ORDER BY start_time;
```

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🗺️ Roadmap

- [ ] **v1.0.0**: -