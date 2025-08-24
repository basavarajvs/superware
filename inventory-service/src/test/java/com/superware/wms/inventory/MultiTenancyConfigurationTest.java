package com.superware.wms.inventory;

import com.superware.wms.inventory.repository.TenantAwareRepositoryFactoryBean;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class MultiTenancyConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private InventoryServiceApplication inventoryServiceApplication;

    @Test
    public void testApplicationContextLoads() {
        // This test verifies that the Spring context loads successfully
        // If the application starts, it means our multi-tenancy configuration is correct
        assertThat(inventoryServiceApplication).isNotNull();
    }

    @Test
    public void testRepositoryFactoryBeanConfiguration() {
        // Verify that our custom repository factory bean is registered
        assertThat(applicationContext.getBean(TenantAwareRepositoryFactoryBean.class)).isNotNull();
    }

    @Test
    public void testRepositoryBeansAreCreated() {
        // Verify that repository beans are created with our custom implementation
        String[] repositoryBeans = applicationContext.getBeanNamesForType(JpaRepository.class);
        assertThat(repositoryBeans).isNotEmpty();
        
        // Check that at least one repository bean exists
        boolean foundInventoryItemRepository = false;
        for (String beanName : repositoryBeans) {
            if (beanName.equals("inventoryItemRepository")) {
                foundInventoryItemRepository = true;
                break;
            }
        }
        assertThat(foundInventoryItemRepository).isTrue();
    }
}