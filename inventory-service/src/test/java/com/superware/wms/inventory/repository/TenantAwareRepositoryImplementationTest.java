package com.superware.wms.inventory.repository;

import com.superware.wms.inventory.InventoryServiceApplication;
import com.superware.wms.inventory.entity.InventoryItem;
import com.superware.wms.tenant.context.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = InventoryServiceApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class TenantAwareRepositoryImplementationTest {

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @AfterEach
    public void tearDown() {
        TenantContextHolder.clear();
        inventoryItemRepository.deleteAll();
    }

    @Test
    public void testTenantFilteringIsolatesDataBetweenTenants() {
        // Set tenant context to tenant 1
        TenantContextHolder.setCurrentTenant("1");

        // Create and save items for tenant 1
        InventoryItem item1 = createTestItem(1, "Product1", "AVAILABLE");
        inventoryItemRepository.save(item1);

        // Create and save items for tenant 2
        TenantContextHolder.setCurrentTenant("2");
        InventoryItem item2 = createTestItem(2, "Product2", "ALLOCATED");
        InventoryItem savedItem2 = inventoryItemRepository.save(item2);

        // Switch back to tenant 1 and verify we only see tenant 1's items
        TenantContextHolder.setCurrentTenant("1");
        List<InventoryItem> tenant1Items = inventoryItemRepository.findAll();
        assertThat(tenant1Items).hasSize(1);
        assertThat(tenant1Items.get(0).getProductId()).isEqualTo(1);

        // Switch to tenant 2 and verify we only see tenant 2's items
        TenantContextHolder.setCurrentTenant("2");
        List<InventoryItem> tenant2Items = inventoryItemRepository.findAll();
        assertThat(tenant2Items).hasSize(1);
        assertThat(tenant2Items.get(0).getItemId()).isEqualTo(savedItem2.getItemId());
        assertThat(tenant2Items.get(0).getProductId()).isEqualTo(2);
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