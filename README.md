# Warehouse Management System (WMS) POC

This is a Proof of Concept (POC) for a Warehouse Management System with multi-tenancy support.

## Overview

This project demonstrates a complete WMS implementation with the following features:

- Multi-tenancy support using a shared database, shared schema approach
- OAuth2 security with Keycloak integration
- RESTful APIs for inventory management
- Comprehensive JPA entity model for warehouse operations
- Spring Boot microservice architecture

## Technologies Used

- Java 21
- Spring Boot 3.3.2
- Spring Data JPA
- PostgreSQL
- Keycloak for authentication
- Maven for build management
- Docker for containerization

## Project Structure

- `common-libraries/` - Shared libraries for tenant context and security
- `inventory-service/` - Main inventory management microservice
- `db/` - Database schema and migration scripts
- `docker-compose.yml` - Docker configuration for PostgreSQL and Keycloak

## Multi-Tenancy Implementation

The multi-tenancy is implemented using a custom repository approach that automatically applies tenant filtering to all database operations. Key components include:

- `TenantContextHolder` - ThreadLocal-based context holder for managing tenant information
- `TenantContextFilter` - Servlet filter that extracts tenant ID from HTTP headers
- `TenantAwareRepository` - Custom repository implementation that applies tenant filtering
- Hibernate filters on all entities to ensure data isolation

## Multi-Tenancy Template

This repository also serves as a template for implementing multi-tenancy in other Spring Boot microservices. The `inventory-service` contains a complete, tested implementation that can be easily adapted for other services.

Key files for the template:
- `TenantAwareRepository.java` - Custom repository implementation
- `TenantAwareRepositoryFactoryBean.java` - Factory bean for creating tenant-aware repositories
- `TenantContextHolder.java` - ThreadLocal-based tenant context management
- `TenantContextFilter.java` - Servlet filter for extracting tenant ID from HTTP headers

Template documentation:
- `MULTI_TENANCY_TEMPLATE.md` - Detailed guide for using this repository as a template
- `create-tenant-service.sh` - Script to create a new service based on this template

## Getting Started

1. Run the environment check script:
   ```bash
   ./check_env.sh
   ```

2. Start the required services using Docker:
   ```bash
   docker-compose up -d
   ```

3. Build the project:
   ```bash
   mvn clean install
   ```

4. Run the inventory service:
   ```bash
   cd inventory-service
   mvn spring-boot:run
   ```

## Testing

Unit and integration tests are included to verify the multi-tenancy implementation. Run tests with:
```bash
mvn test
```

To create a new service based on the multi-tenancy template:
```bash
./create-tenant-service.sh order-service
```

## Documentation

- `WMS_POC_SETUP.md` - Detailed setup instructions
- `WMS_POC_PROGRESS.md` - Step-by-step implementation log
- `API_DOCUMENTATION.md` - API endpoints documentation
- `MULTI_TENANCY_VERIFICATION.md` - Multi-tenancy implementation verification
- `MULTI_TENANCY_TEMPLATE.md` - Guide for using this repository as a template for other services

## License

This project is for educational and demonstration purposes only.