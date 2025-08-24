package com.superware.wms.inventory.repository;

import com.superware.wms.inventory.InventoryServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = InventoryServiceApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class TenantAwareRepositoryFactoryBeanSpringBootTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Test
    public void testRepositoryFactoryBeanIsUsed() {
        // Get the repository factory bean from the application context
        Object factoryBean = applicationContext.getBean("&inventoryItemRepository");
        
        // Verify that our custom factory bean is used
        assertThat(factoryBean).isInstanceOf(TenantAwareRepositoryFactoryBean.class);
    }

    @Test
    public void testRepositoryImplementation() {
        // Verify that the repository is using our custom implementation
        assertThat(inventoryItemRepository).isInstanceOf(TenantAwareRepository.class);
    }
}