package com.superware.wms.inventory;

import com.superware.wms.inventory.repository.TenantAwareRepositoryFactoryBean;
import com.superware.wms.tenant.context.TenantContextHolder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(
    basePackages = "com.superware.wms.inventory.repository",
    repositoryFactoryBeanClass = TenantAwareRepositoryFactoryBean.class
)
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
        System.out.println("Test application started successfully!");
        TenantContextHolder.clear();
    }
}