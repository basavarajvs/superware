package com.superware.wms.inventory;

import com.superware.wms.inventory.entity.InventoryItem;
import com.superware.wms.inventory.repository.InventoryItemRepository;
import com.superware.wms.inventory.repository.TenantAwareRepositoryFactoryBean;
import com.superware.wms.tenant.context.TenantContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
@EnableJpaRepositories(
    basePackages = "com.superware.wms.inventory.repository",
    repositoryFactoryBeanClass = TenantAwareRepositoryFactoryBean.class
)
public class ManualTenantTest implements CommandLineRunner {

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    public static void main(String[] args) {
        SpringApplication.run(ManualTenantTest.class, args);
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("Starting manual tenant filtering test...");

        // Clear any existing data
        TenantContextHolder.setCurrentTenant("1");
        inventoryItemRepository.deleteAll();
        TenantContextHolder.setCurrentTenant("2");
        inventoryItemRepository.deleteAll();

        // Test tenant 1
        TenantContextHolder.setCurrentTenant("1");
        InventoryItem item1 = createTestItem(1, "Product1", "AVAILABLE");
        inventoryItemRepository.save(item1);
        System.out.println("Saved item for tenant 1");

        // Test tenant 2
        TenantContextHolder.setCurrentTenant("2");
        InventoryItem item2 = createTestItem(2, "Product2", "ALLOCATED");
        InventoryItem savedItem2 = inventoryItemRepository.save(item2);
        System.out.println("Saved item for tenant 2");

        // Verify tenant isolation
        TenantContextHolder.setCurrentTenant("1");
        List<InventoryItem> tenant1Items = inventoryItemRepository.findAll();
        System.out.println("Tenant 1 items count: " + tenant1Items.size());
        if (tenant1Items.size() == 1 && tenant1Items.get(0).getProductId().equals(1)) {
            System.out.println("✓ Tenant 1 filtering works correctly");
        } else {
            System.out.println("✗ Tenant 1 filtering failed");
        }

        TenantContextHolder.setCurrentTenant("2");
        List<InventoryItem> tenant2Items = inventoryItemRepository.findAll();
        System.out.println("Tenant 2 items count: " + tenant2Items.size());
        if (tenant2Items.size() == 1 && tenant2Items.get(0).getItemId().equals(savedItem2.getItemId()) 
            && tenant2Items.get(0).getProductId().equals(2)) {
            System.out.println("✓ Tenant 2 filtering works correctly");
        } else {
            System.out.println("✗ Tenant 2 filtering failed");
        }

        // Clean up
        TenantContextHolder.setCurrentTenant("1");
        inventoryItemRepository.deleteAll();
        TenantContextHolder.setCurrentTenant("2");
        inventoryItemRepository.deleteAll();

        TenantContextHolder.clear();
        System.out.println("Manual tenant filtering test completed!");
    }

    private InventoryItem createTestItem(Integer productId, String productName, String status) {
        InventoryItem item = new InventoryItem();
        item.setProductId(productId);
        item.setLotNumber("LOT-" + productName);
        item.setStatus(status);
        item.setQuantityOnHand(new BigDecimal("100.00"));
        item.setUnitOfMeasure("PCS");
        item.setFacilityId(1);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        item.setCreatedBy(1);
        item.setUpdatedBy(1);
        item.setIsActive(true);
        item.setIsDeleted(false);
        return item;
    }
}