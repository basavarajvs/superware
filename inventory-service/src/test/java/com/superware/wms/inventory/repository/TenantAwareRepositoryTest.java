package com.superware.wms.inventory.repository;

import com.superware.wms.inventory.InventoryServiceApplication;
import com.superware.wms.inventory.entity.InventoryItem;
import com.superware.wms.tenant.context.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = InventoryServiceApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class TenantAwareRepositoryTest {

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @BeforeEach
    public void setUp() {
        // Clear any existing tenant context
        TenantContextHolder.clear();
    }

    @AfterEach
    public void tearDown() {
        // Clean up test data and clear tenant context
        TenantContextHolder.clear();
        inventoryItemRepository.deleteAll();
    }

    @Test
    public void testTenantFilteringWithTenant1() {
        // Set tenant context to tenant 1
        TenantContextHolder.setCurrentTenant("1");

        // Create and save items for tenant 1
        InventoryItem item1 = createTestItem(1, "Product1", "AVAILABLE");
        InventoryItem savedItem1 = inventoryItemRepository.save(item1);

        // Create and save items for tenant 2
        TenantContextHolder.setCurrentTenant("2");
        InventoryItem item2 = createTestItem(2, "Product2", "ALLOCATED");
        inventoryItemRepository.save(item2);

        // Switch back to tenant 1 and verify we only see tenant 1's items
        TenantContextHolder.setCurrentTenant("1");
        List<InventoryItem> tenant1Items = inventoryItemRepository.findAll();
        assertThat(tenant1Items).hasSize(1);
        assertThat(tenant1Items.get(0).getItemId()).isEqualTo(savedItem1.getItemId());
        assertThat(tenant1Items.get(0).getProductId()).isEqualTo(1);
    }

    @Test
    public void testTenantFilteringWithTenant2() {
        // Set tenant context to tenant 1
        TenantContextHolder.setCurrentTenant("1");

        // Create and save items for tenant 1
        InventoryItem item1 = createTestItem(1, "Product1", "AVAILABLE");
        inventoryItemRepository.save(item1);

        // Create and save items for tenant 2
        TenantContextHolder.setCurrentTenant("2");
        InventoryItem item2 = createTestItem(2, "Product2", "ALLOCATED");
        InventoryItem savedItem2 = inventoryItemRepository.save(item2);

        // Verify we only see tenant 2's items
        List<InventoryItem> tenant2Items = inventoryItemRepository.findAll();
        assertThat(tenant2Items).hasSize(1);
        assertThat(tenant2Items.get(0).getItemId()).isEqualTo(savedItem2.getItemId());
        assertThat(tenant2Items.get(0).getProductId()).isEqualTo(2);
    }

    @Test
    public void testFindByIdWithTenantFiltering() {
        // Set tenant context to tenant 1
        TenantContextHolder.setCurrentTenant("1");

        // Create and save an item for tenant 1
        InventoryItem item1 = createTestItem(1, "Product1", "AVAILABLE");
        InventoryItem savedItem1 = inventoryItemRepository.save(item1);

        // Create and save an item for tenant 2 with the same ID sequence
        TenantContextHolder.setCurrentTenant("2");
        InventoryItem item2 = createTestItem(2, "Product2", "ALLOCATED");
        InventoryItem savedItem2 = inventoryItemRepository.save(item2);

        // Verify tenant 1 can only see its own item
        TenantContextHolder.setCurrentTenant("1");
        Optional<InventoryItem> foundItem1 = inventoryItemRepository.findById(savedItem1.getItemId());
        assertThat(foundItem1).isPresent();
        assertThat(foundItem1.get().getProductId()).isEqualTo(1);

        // Verify tenant 1 cannot see tenant 2's item
        Optional<InventoryItem> notFoundItem2 = inventoryItemRepository.findById(savedItem2.getItemId());
        assertThat(notFoundItem2).isNotPresent();

        // Verify tenant 2 can only see its own item
        TenantContextHolder.setCurrentTenant("2");
        Optional<InventoryItem> foundItem2 = inventoryItemRepository.findById(savedItem2.getItemId());
        assertThat(foundItem2).isPresent();
        assertThat(foundItem2.get().getProductId()).isEqualTo(2);

        // Verify tenant 2 cannot see tenant 1's item
        Optional<InventoryItem> notFoundItem1 = inventoryItemRepository.findById(savedItem1.getItemId());
        assertThat(notFoundItem1).isNotPresent();
    }

    @Test
    public void testSaveWithTenantFiltering() {
        // Set tenant context to tenant 1
        TenantContextHolder.setCurrentTenant("1");

        // Create and save an item
        InventoryItem item = createTestItem(1, "Product1", "AVAILABLE");
        InventoryItem savedItem = inventoryItemRepository.save(item);

        // Verify the tenant ID was set correctly
        assertThat(savedItem.getTenantId()).isEqualTo(1);

        // Verify we can find the item
        Optional<InventoryItem> foundItem = inventoryItemRepository.findById(savedItem.getItemId());
        assertThat(foundItem).isPresent();
        assertThat(foundItem.get().getTenantId()).isEqualTo(1);
    }

    @Test
    public void testUpdateWithTenantFiltering() {
        // Set tenant context to tenant 1
        TenantContextHolder.setCurrentTenant("1");

        // Create and save an item
        InventoryItem item = createTestItem(1, "Product1", "AVAILABLE");
        InventoryItem savedItem = inventoryItemRepository.save(item);

        // Update the item
        savedItem.setStatus("ALLOCATED");
        InventoryItem updatedItem = inventoryItemRepository.save(savedItem);

        // Verify the update was successful
        assertThat(updatedItem.getStatus()).isEqualTo("ALLOCATED");

        // Verify tenant 2 cannot see this item
        TenantContextHolder.setCurrentTenant("2");
        Optional<InventoryItem> notFoundItem = inventoryItemRepository.findById(savedItem.getItemId());
        assertThat(notFoundItem).isNotPresent();
    }

    @Test
    public void testDeleteWithTenantFiltering() {
        // Set tenant context to tenant 1
        TenantContextHolder.setCurrentTenant("1");

        // Create and save an item
        InventoryItem item = createTestItem(1, "Product1", "AVAILABLE");
        InventoryItem savedItem = inventoryItemRepository.save(item);

        // Verify the item exists
        assertThat(inventoryItemRepository.findById(savedItem.getItemId())).isPresent();

        // Delete the item
        inventoryItemRepository.deleteById(savedItem.getItemId());

        // Verify the item is deleted for tenant 1
        assertThat(inventoryItemRepository.findById(savedItem.getItemId())).isNotPresent();

        // Verify tenant 2 was never able to see this item
        TenantContextHolder.setCurrentTenant("2");
        assertThat(inventoryItemRepository.findById(savedItem.getItemId())).isNotPresent();
    }

    @Test
    public void testCustomQueriesWithTenantFiltering() {
        // Set tenant context to tenant 1
        TenantContextHolder.setCurrentTenant("1");

        // Create and save items for tenant 1
        InventoryItem item1 = createTestItem(1, "Product1", "AVAILABLE");
        inventoryItemRepository.save(item1);

        InventoryItem item2 = createTestItem(1, "Product1", "ALLOCATED");
        inventoryItemRepository.save(item2);

        // Create items for tenant 2
        TenantContextHolder.setCurrentTenant("2");
        InventoryItem item3 = createTestItem(2, "Product1", "AVAILABLE");
        inventoryItemRepository.save(item3);

        // Test custom query method with tenant filtering
        TenantContextHolder.setCurrentTenant("1");
        List<InventoryItem> tenant1ProductItems = inventoryItemRepository.findByProductId(1);
        assertThat(tenant1ProductItems).hasSize(2);
        assertThat(tenant1ProductItems).allMatch(item -> item.getTenantId().equals(1));

        List<InventoryItem> tenant1AvailableItems = inventoryItemRepository.findByStatus("AVAILABLE");
        assertThat(tenant1AvailableItems).hasSize(1);
        assertThat(tenant1AvailableItems.get(0).getTenantId()).isEqualTo(1);

        // Test with tenant 2
        TenantContextHolder.setCurrentTenant("2");
        List<InventoryItem> tenant2ProductItems = inventoryItemRepository.findByProductId(2);
        assertThat(tenant2ProductItems).hasSize(1);
        assertThat(tenant2ProductItems.get(0).getTenantId()).isEqualTo(2);
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