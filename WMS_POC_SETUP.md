# WMS POC Project Setup Documentation

## Project Overview
This document summarizes all the setup steps and configurations performed for the Warehouse Management System (WMS) Proof of Concept project.

## Environment Setup

### 1. Environment Check Script
Created a bash script `check_env.sh` to verify required tools:
- Java (version 21)
- Maven
- Docker

Script location: `/check_env.sh`

To run the script:
```bash
./check_env.sh
```

## Docker Configuration

### 2. Docker Compose Setup
Created `docker-compose.yml` with two services:

1. **PostgreSQL Database**
   - Image: postgres:16
   - Container name: wms-postgres
   - Environment:
     - POSTGRES_USER: wmsadmin
     - POSTGRES_PASSWORD: wmsadminpass
     - POSTGRES_DB: wms_db
   - Port mapping: 5432:5432
   - Volume: postgres-data for persistent storage

2. **Keycloak Authentication**
   - Image: quay.io/keycloak/keycloak:25.0.2
   - Container name: wms-keycloak
   - Environment:
     - KC_DB: dev-file
     - KEYCLOAK_ADMIN: admin
     - KEYCLOAK_ADMIN_PASSWORD: admin123
   - Port mapping: 8080:8080
   - Command: start-dev

To start all services:
```bash
docker-compose up -d
```

To stop all services:
```bash
docker-compose down
```

## Maven Project Structure

### 3. Multi-module Maven Project
Created a parent POM with the following structure:

```
wms-poc (parent)
├── common-libraries (pom)
│   ├── wms-tenant-context (jar)
│   └── wms-security-commons (jar)
└── inventory-service (jar)
```

#### Parent POM
- Group ID: com.superware.wms
- Artifact ID: wms-poc
- Version: 1.0.0-SNAPSHOT
- Java Version: 21
- Spring Boot Version: 3.3.2

#### Common Libraries Module
Provides shared functionality across services.

##### WMS Tenant Context
- Artifact ID: wms-tenant-context
- Contains TenantContextHolder class using ThreadLocal
- Contains TenantContextFilter servlet filter

#### WMS Security Commons
- Artifact ID: wms-security-commons
- Dependencies on Spring Security and OAuth2 Resource Server
- Auto-configuration for OAuth2 Resource Server with JWT validation
- Pre-configured to work with Keycloak at http://localhost:8080/realms/wms-realm
- Requires authentication for all requests by default

#### Inventory Service
- Artifact ID: inventory-service
- Dependencies on Spring Boot Web, Data JPA
- Dependencies on local common libraries

To build the entire project:
```bash
mvn clean install
```

To verify the project structure is correct:
```bash
mvn validate
```

## Multi-tenancy Implementation

### 4. Tenant Context Implementation

#### TenantContextHolder Class
Location: `/common-libraries/wms-tenant-context/src/main/java/com/superware/wms/tenant/context/TenantContextHolder.java`

Provides static methods:
- `setCurrentTenant(String tenantId)` - Sets current tenant ID
- `getCurrentTenant()` - Gets current tenant ID
- `clear()` - Clears tenant context

Implementation uses ThreadLocal for thread-safe storage.

#### TenantContextFilter
Location: `/common-libraries/wms-tenant-context/src/main/java/com/superware/wms/tenant/context/filter/TenantContextFilter.java`

Servlet filter that:
- Extracts 'X-Tenant-ID' header from HTTP requests
- Sets tenant context using TenantContextHolder
- Ensures context is cleared after request processing
- Handles exceptions properly to prevent memory leaks

## OAuth2 Security Implementation

### 5. Spring Security Configuration

#### WmsSecurityConfig Class
Location: `/common-libraries/wms-security-commons/src/main/java/com/superware/wms/security/config/WmsSecurityConfig.java`

Security configuration that:
- Enables stateless session management
- Requires authentication for all requests
- Configures OAuth2 Resource Server with JWT validation
- Points to Keycloak's JWK Set URL for token validation
- Uses Spring Security's new lambda DSL for configuration

#### Auto-configuration
The library includes auto-configuration through:
- `WmsSecurityAutoConfig.java` - Imports the main configuration
- `META-INF/spring.factories` - Registers the auto-configuration

When included as a dependency in a Spring Boot microservice, security is automatically configured.

#### Maven Dependencies
The library includes:
- `spring-boot-starter-security` - Core Spring Security
- `spring-boot-starter-oauth2-resource-server` - OAuth2 Resource Server support

To use in a microservice, simply add the dependency:
```xml
<dependency>
    <groupId>com.superware.wms</groupId>
    <artifactId>wms-security-commons</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Keycloak Configuration

### 6. Authentication Setup

#### Keycloak Access
- Admin Console: http://localhost:8080/admin
- Admin Credentials: admin / admin123

#### Realm and Client Configuration
You have successfully created:
- Realm Name: wms-realm (custom name)
- Client ID: wms-client (custom name)
- Client Secret: Generated in Keycloak admin console

#### Manual Setup Steps (for reference)
1. Access Keycloak admin console at http://localhost:8080/admin
2. Log in with admin credentials (admin/admin123)
3. Create a new realm named 'wms-realm'
4. In the realm, create a client named 'wms-client'
5. Set the client's access type to 'confidential'
6. Save the client and go to the 'Credentials' tab to get the client secret

#### Access Token Request
Successful curl command to retrieve access token:
```bash
curl -X POST \\
  http://localhost:8080/realms/wms-realm/protocol/openid-connect/token \\
  -H 'Content-Type: application/x-www-form-urlencoded' \\
  -d 'client_id=wms-client' \\
  -d 'client_secret=YOUR_CLIENT_SECRET' \\
  -d 'grant_type=client_credentials'
```

Replace `YOUR_CLIENT_SECRET` with the actual client secret from Keycloak.

The `wms-security-commons` library is configured to validate JWT tokens issued by this Keycloak instance.

## Project Structure Summary

```
SuperWare/
├── check_env.sh
├── docker-compose.yml
├── pom.xml (parent)
├── KEYCLOAK.md
├── WMS_POC_SETUP.md
├── common-libraries/
│   ├── pom.xml
│   ├── wms-tenant-context/
│   │   ├── pom.xml
│   │   └── src/main/java/com/superware/wms/tenant/context/
│   │       ├── TenantContextHolder.java
│   │       └── filter/TenantContextFilter.java
│   └── wms-security-commons/
│       ├── pom.xml
│       └── src/main/java/com/superware/wms/security/config/
│           ├── WmsSecurityConfig.java
│           ├── WmsSecurityAutoConfig.java
│           └── META-INF/spring.factories
└── inventory-service/
    └── pom.xml
```

## Services Status

After running `docker-compose up -d`:

1. **PostgreSQL Database**: Running on port 5432
   - Accessible at: localhost:5432
   - Credentials: wmsadmin / wmsadminpass
   - Database: wms_db

2. **Keycloak**: Running on port 8080
   - Admin Console: http://localhost:8080/admin
   - Credentials: admin / admin123
   - Realm: wms-realm
   - Client: wms-client

You have confirmed that Keycloak is working correctly and the curl request to access tokens is successful.

The `wms-security-commons` library has been implemented and can be used in microservices to secure them with OAuth2 JWT validation.

## Verification Commands

To verify all components are working correctly:

1. **Check environment**:
   ```bash
   ./check_env.sh
   ```

2. **Validate Maven project structure**:
   ```bash
   mvn validate
   ```

3. **Compile tenant context module**:
   ```bash
   mvn compile -pl common-libraries/wms-tenant-context
   ```

4. **Compile security commons module**:
   ```bash
   mvn compile -pl common-libraries/wms-security-commons
   ```

5. **Build and install common libraries**:
   ```bash
   mvn clean install -pl common-libraries/wms-tenant-context
   mvn clean install -pl common-libraries/wms-security-commons
   ```

6. **Check Docker containers status**:
   ```bash
   docker-compose ps
   ```

7. **Validate Docker Compose configuration**:
   ```bash
   docker-compose config
   ```

## Next Steps

1. Implement additional functionality in the inventory service
2. Add more modules to the common libraries as needed
3. Configure additional Keycloak settings (users, roles, etc.)
4. Integrate tenant context with database operations
5. Use the wms-security-commons library in microservices
6. Add role-based access control to the security configuration
7. Implement additional security features like CORS configuration