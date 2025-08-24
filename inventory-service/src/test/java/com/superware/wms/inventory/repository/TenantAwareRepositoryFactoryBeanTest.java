package com.superware.wms.inventory.repository;

import com.superware.wms.inventory.config.TestRepositoryConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestRepositoryConfig.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TenantAwareRepositoryFactoryBeanTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Test
    public void testRepositoryFactoryBeanIsUsed() {
        // Get the repository factory bean from the application context
        JpaRepositoryFactoryBean<?, ?, ?> factoryBean = applicationContext.getBean(
            "inventoryItemRepository", JpaRepositoryFactoryBean.class);
        
        // Verify that our custom factory bean is used
        assertThat(factoryBean).isInstanceOf(TenantAwareRepositoryFactoryBean.class);
    }

    @Test
    public void testRepositoryImplementation() {
        // Verify that the repository is using our custom implementation
        assertThat(inventoryItemRepository).isInstanceOf(TenantAwareRepository.class);
    }
}