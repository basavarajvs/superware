package com.superware.wms.inventory.config;

import com.superware.wms.inventory.repository.TenantAwareRepositoryFactoryBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "com.superware.wms.inventory.entity")
@EnableJpaRepositories(
    basePackages = "com.superware.wms.inventory.repository",
    repositoryFactoryBeanClass = TenantAwareRepositoryFactoryBean.class
)
public class TestRepositoryConfig {
}