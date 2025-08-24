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
        // Verify that the repository implements our TenantAwareRepository interface
        assertThat(TenantAwareRepository.class.isAssignableFrom(inventoryItemRepository.getClass())).isTrue();
        
        // Also verify the actual implementation class
        System.out.println("Repository class: " + inventoryItemRepository.getClass().getName());
        System.out.println("Repository interfaces: " + java.util.Arrays.toString(inventoryItemRepository.getClass().getInterfaces()));
        
        // Verify that TenantAwareRepository is one of the implemented interfaces
        boolean implementsTenantAware = false;
        for (Class<?> interfaceClass : inventoryItemRepository.getClass().getInterfaces()) {
            if (TenantAwareRepository.class.isAssignableFrom(interfaceClass)) {
                implementsTenantAware = true;
                break;
            }
        }
        assertThat(implementsTenantAware).isTrue();
    }
}