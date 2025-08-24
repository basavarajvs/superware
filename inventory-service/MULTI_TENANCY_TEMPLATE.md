# Multi-Tenancy Repository Template

This repository serves as a template for implementing multi-tenancy in Spring Boot microservices. It provides a complete implementation of shared-schema, shared-database multi-tenancy with automatic tenant filtering.

## Features

- **Automatic Tenant Filtering**: All repository operations automatically apply tenant filtering
- **Transparent Implementation**: Service layer doesn't need to know about multi-tenancy
- **Thread-Safe**: Uses ThreadLocal for tenant context management
- **Servlet Filter Integration**: Automatically extracts tenant ID from HTTP headers
- **Custom Repository Implementation**: Extends Spring Data JPA with tenant-aware repositories

## Core Components

### 1. TenantAwareRepository
A custom repository implementation that extends `SimpleJpaRepository` and automatically applies tenant filtering to all operations:

```java
public class TenantAwareRepository<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> {
    // Overrides all repository methods to enable tenant filtering
}
```

### 2. TenantAwareRepositoryFactoryBean
A custom factory bean that tells Spring Data JPA to use our `TenantAwareRepository` implementation:

```java
@EnableJpaRepositories(
    basePackages = "com.superware.wms.inventory.repository",
    repositoryFactoryBeanClass = TenantAwareRepositoryFactoryBean.class
)
```

### 3. TenantContextHolder
ThreadLocal-based context holder for managing tenant information:

```java
public class TenantContextHolder {
    private static final ThreadLocal<String> tenantContext = new ThreadLocal<>();
    
    public static void setCurrentTenant(String tenantId) {
        tenantContext.set(tenantId);
    }
    
    public static String getCurrentTenant() {
        return tenantContext.get();
    }
    
    public static void clear() {
        tenantContext.remove();
    }
}
```

### 4. TenantContextFilter
Servlet filter that extracts tenant ID from HTTP headers:

```java
public class TenantContextFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        try {
            if (request instanceof HttpServletRequest) {
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                String tenantId = httpRequest.getHeader("X-Tenant-ID");
                TenantContextHolder.setCurrentTenant(tenantId);
            }
            chain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();
        }
    }
}
```

## Usage in Other Services

To use this template in other services:

1. **Copy the core components**:
   - `TenantAwareRepository.java`
   - `TenantAwareRepositoryFactoryBean.java`
   - `TenantContextHolder.java`
   - `TenantContextFilter.java`

2. **Configure your main application class**:
```java
@SpringBootApplication
@EnableJpaRepositories(
    basePackages = "com.yourcompany.yourservice.repository",
    repositoryFactoryBeanClass = TenantAwareRepositoryFactoryBean.class
)
public class YourServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourServiceApplication.class, args);
    }
}
```

3. **Create your base repository interface**:
```java
@NoRepositoryBean
public interface YourBaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {
}
```

4. **Create your entity repositories**:
```java
@Repository
public interface YourEntityRepository extends YourBaseRepository<YourEntity, Long> {
    // Your custom query methods
}
```

5. **Add Hibernate filters to your entities**:
```java
@Entity
@Table(name = "your_entities")
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class YourEntity {
    // Entity fields
}
```

6. **Register the TenantContextFilter** in your configuration:
```java
@Configuration
public class WebConfig {
    @Bean
    public FilterRegistrationBean<TenantContextFilter> tenantContextFilter() {
        FilterRegistrationBean<TenantContextFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TenantContextFilter());
        registrationBean.addUrlPatterns("/api/*");
        return registrationBean;
    }
}
```

## Testing

The template includes comprehensive tests to verify the multi-tenancy implementation:

1. **Unit Tests**:
   - `TenantContextHolderTest`: Tests the tenant context holder functionality
   - `TenantContextFilterTest`: Tests the tenant context filter behavior

2. **Integration Tests**:
   - `TenantAwareRepositorySpringBootTest`: Tests the repository implementation with tenant filtering
   - `HibernateFilterSpringBootTest`: Tests that Hibernate filters are correctly applied
   - `TenantAwareRepositoryFactoryBeanSpringBootTest`: Tests that the custom factory bean is used

## Running Tests

To run all tests:
```bash
mvn test
```

To run specific tests:
```bash
mvn test -Dtest=TenantContextHolderTest
mvn test -Dtest=TenantContextFilterTest
mvn test -Dtest=ApplicationContextLoadTest
```

## Verification

The implementation has been verified to:

1. ✅ Correctly load the Spring application context with multi-tenancy configuration
2. ✅ Properly inject repositories with the custom `TenantAwareRepository` implementation
3. ✅ Automatically apply tenant filtering to all repository operations
4. ✅ Isolate data between different tenants
5. ✅ Extract tenant ID from HTTP headers using the servlet filter
6. ✅ Maintain thread safety with ThreadLocal context management

## Best Practices

1. **Always extend your base repository** from the custom implementation to ensure tenant filtering
2. **Add Hibernate filters** to all entities that need tenant isolation
3. **Use the servlet filter** to automatically extract tenant ID from requests
4. **Test thoroughly** to ensure tenant isolation is working correctly
5. **Handle tenant context clearing** properly to prevent memory leaks

## Limitations

1. This implementation assumes tenant ID is passed in the `X-Tenant-ID` HTTP header
2. Only supports shared-schema, shared-database multi-tenancy strategy
3. Requires all tenant-aware entities to have a `tenant_id` column