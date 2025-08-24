#!/bin/bash

# Script to set up multi-tenancy template for a new service

echo "Setting up multi-tenancy template for a new service..."

# Check if service name is provided
if [ $# -eq 0 ]; then
    echo "Usage: $0 <service-name>"
    echo "Example: $0 order-service"
    exit 1
fi

SERVICE_NAME=$1
echo "Creating multi-tenancy template for $SERVICE_NAME..."

# Create directory structure
mkdir -p $SERVICE_NAME/src/main/java/com/superware/wms/$SERVICE_NAME/repository
mkdir -p $SERVICE_NAME/src/main/java/com/superware/wms/$SERVICE_NAME/entity
mkdir -p $SERVICE_NAME/src/main/java/com/superware/wms/$SERVICE_NAME/config
mkdir -p $SERVICE_NAME/src/main/resources
mkdir -p $SERVICE_NAME/src/test/java/com/superware/wms/$SERVICE_NAME

# Copy core multi-tenancy components
cp -r inventory-service/src/main/java/com/superware/wms/inventory/repository/TenantAwareRepository.java $SERVICE_NAME/src/main/java/com/superware/wms/$SERVICE_NAME/repository/
cp -r inventory-service/src/main/java/com/superware/wms/inventory/repository/TenantAwareRepositoryFactoryBean.java $SERVICE_NAME/src/main/java/com/superware/wms/$SERVICE_NAME/repository/

# Create base repository interface
cat > $SERVICE_NAME/src/main/java/com/superware/wms/$SERVICE_NAME/repository/BaseRepository.java << 'EOF'
package com.superware.wms.'$SERVICE_NAME'.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * Base repository interface for all repositories in this service.
 */
@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {
}
EOF

# Create main application class
cat > $SERVICE_NAME/src/main/java/com/superware/wms/$SERVICE_NAME/'${SERVICE_NAME^}'Application.java << 'EOF'
package com.superware.wms.'$SERVICE_NAME';

import com.superware.wms.'$SERVICE_NAME'.repository.TenantAwareRepositoryFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(
    basePackages = "com.superware.wms.'$SERVICE_NAME'.repository",
    repositoryFactoryBeanClass = TenantAwareRepositoryFactoryBean.class
)
public class '${SERVICE_NAME^}'Application {

    public static void main(String[] args) {
        SpringApplication.run('${SERVICE_NAME^}'Application.class, args);
    }
}
EOF

# Create pom.xml
cat > $SERVICE_NAME/pom.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.superware.wms</groupId>
        <artifactId>wms-poc</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>'$SERVICE_NAME'</artifactId>
    <packaging>jar</packaging>

    <name>'${SERVICE_NAME^}'</name>
    <description>'${SERVICE_NAME^}' for WMS</description>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        
        <dependency>
            <groupId>com.superware.wms</groupId>
            <artifactId>wms-tenant-context</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
        
        <!-- Test dependencies -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <goals>
                            <goal>repackage</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
EOF

echo "Multi-tenancy template for $SERVICE_NAME created successfully!"
echo "Next steps:"
echo "1. Add your entity classes to src/main/java/com/superware/wms/$SERVICE_NAME/entity/"
echo "2. Add Hibernate filters to your entities"
echo "3. Create repository interfaces extending BaseRepository"
echo "4. Add custom query methods to your repositories"
echo "5. Build and test your service: cd $SERVICE_NAME && mvn clean install"