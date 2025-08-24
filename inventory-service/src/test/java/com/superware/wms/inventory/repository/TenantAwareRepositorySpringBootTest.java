package com.superware.wms.inventory.repository;

import com.superware.wms.inventory.entity.InventoryItem;
import com.superware.wms.tenant.context.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class TenantAwareRepositorySpringBootTest {

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
    }

    @Test
    public void testRepositoryUsesTenantAwareImplementation() {
        // Verify that the repository implements our TenantAwareRepository interface
        // This checks if the repository proxy implements the interface
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

    @Test
    public void testTenantFilteringWithTenant1() {
        // Set tenant context to tenant 1
        TenantContextHolder.setCurrentTenant("1");

        // Create and save items for tenant 1
        InventoryItem item1 = createTestItem(1, "Product1", "AVAILABLE");
        InventoryItem savedItem1 = inventoryItemRepository.save(item1);
        
        // Verify the saved item has the correct tenant ID
        assertThat(savedItem1.getTenantId()).isEqualTo(1);

        // Create and save items for tenant 2
        TenantContextHolder.setCurrentTenant("2");
        InventoryItem item2 = createTestItem(2, "Product2", "ALLOCATED");
        InventoryItem savedItem2 = inventoryItemRepository.save(item2);
        
        // Verify the saved item has the correct tenant ID
        assertThat(savedItem2.getTenantId()).isEqualTo(2);

        // Switch back to tenant 1 and verify we only see tenant 1's items
        TenantContextHolder.setCurrentTenant("1");
        List<InventoryItem> tenant1Items = inventoryItemRepository.findAll();
        assertThat(tenant1Items).hasSize(1);
        assertThat(tenant1Items.get(0).getItemId()).isEqualTo(savedItem1.getItemId());
        assertThat(tenant1Items.get(0).getProductId()).isEqualTo(1);
        assertThat(tenant1Items.get(0).getTenantId()).isEqualTo(1);
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
        
        // Verify the saved item has the correct tenant ID
        assertThat(savedItem2.getTenantId()).isEqualTo(2);

        // Verify we only see tenant 2's items
        List<InventoryItem> tenant2Items = inventoryItemRepository.findAll();
        assertThat(tenant2Items).hasSize(1);
        assertThat(tenant2Items.get(0).getItemId()).isEqualTo(savedItem2.getItemId());
        assertThat(tenant2Items.get(0).getProductId()).isEqualTo(2);
        assertThat(tenant2Items.get(0).getTenantId()).isEqualTo(2);
    }

    @Test
    public void testFindByIdWithTenantFiltering() {
        // Set tenant context to tenant 1
        TenantContextHolder.setCurrentTenant("1");

        // Create and save an item for tenant 1
        InventoryItem item1 = createTestItem(1, "Product1", "AVAILABLE");
        InventoryItem savedItem1 = inventoryItemRepository.save(item1);
        
        // Verify the saved item has the correct tenant ID
        assertThat(savedItem1.getTenantId()).isEqualTo(1);

        // Create and save an item for tenant 2 with the same ID sequence
        TenantContextHolder.setCurrentTenant("2");
        InventoryItem item2 = createTestItem(2, "Product2", "ALLOCATED");
        InventoryItem savedItem2 = inventoryItemRepository.save(item2);
        
        // Verify the saved item has the correct tenant ID
        assertThat(savedItem2.getTenantId()).isEqualTo(2);

        // Verify tenant 1 can only see its own item
        TenantContextHolder.setCurrentTenant("1");
        Optional<InventoryItem> foundItem1 = inventoryItemRepository.findById(savedItem1.getItemId());
        assertThat(foundItem1).isPresent();
        assertThat(foundItem1.get().getProductId()).isEqualTo(1);
        assertThat(foundItem1.get().getTenantId()).isEqualTo(1);

        // Verify tenant 1 cannot see tenant 2's item
        Optional<InventoryItem> notFoundItem2 = inventoryItemRepository.findById(savedItem2.getItemId());
        assertThat(notFoundItem2).isNotPresent();

        // Verify tenant 2 can only see its own item
        TenantContextHolder.setCurrentTenant("2");
        Optional<InventoryItem> foundItem2 = inventoryItemRepository.findById(savedItem2.getItemId());
        assertThat(foundItem2).isPresent();
        assertThat(foundItem2.get().getProductId()).isEqualTo(2);
        assertThat(foundItem2.get().getTenantId()).isEqualTo(2);

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
        assertThat(savedItem.getTenantId()).isEqualTo(1);

        // Update the item
        savedItem.setStatus("ALLOCATED");
        InventoryItem updatedItem = inventoryItemRepository.save(savedItem);
        assertThat(updatedItem.getStatus()).isEqualTo("ALLOCATED");
        assertThat(updatedItem.getTenantId()).isEqualTo(1);

        // Verify the update persisted
        Optional<InventoryItem> foundItem = inventoryItemRepository.findById(savedItem.getItemId());
        assertThat(foundItem).isPresent();
        assertThat(foundItem.get().getStatus()).isEqualTo("ALLOCATED");
        assertThat(foundItem.get().getTenantId()).isEqualTo(1);
    }

    @Test
    public void testDeleteWithTenantFiltering() {
        // Set tenant context to tenant 1
        TenantContextHolder.setCurrentTenant("1");

        // Create and save an item
        InventoryItem item = createTestItem(1, "Product1", "AVAILABLE");
        InventoryItem savedItem = inventoryItemRepository.save(item);
        assertThat(savedItem.getTenantId()).isEqualTo(1);

        // Delete the item
        inventoryItemRepository.deleteById(savedItem.getItemId());

        // Verify the item is deleted
        Optional<InventoryItem> foundItem = inventoryItemRepository.findById(savedItem.getItemId());
        assertThat(foundItem).isNotPresent();
    }

    @Test
    public void testCustomQueriesWithTenantFiltering() {
        // Set tenant context to tenant 1
        TenantContextHolder.setCurrentTenant("1");

        // Create and save items for tenant 1
        InventoryItem item1 = createTestItem(1, "Product1", "AVAILABLE");
        InventoryItem item2 = createTestItem(2, "Product2", "ALLOCATED");
        inventoryItemRepository.save(item1);
        inventoryItemRepository.save(item2);

        // Create items for tenant 2
        TenantContextHolder.setCurrentTenant("2");
        InventoryItem item3 = createTestItem(3, "Product3", "AVAILABLE");
        inventoryItemRepository.save(item3);

        // Switch back to tenant 1 and test custom queries
        TenantContextHolder.setCurrentTenant("1");
        
        // Test findByStatus
        List<InventoryItem> availableItems = inventoryItemRepository.findByStatus("AVAILABLE");
        assertThat(availableItems).hasSize(1);
        assertThat(availableItems.get(0).getTenantId()).isEqualTo(1);
        
        // Test findByProductId
        List<InventoryItem> product1Items = inventoryItemRepository.findByProductId(1);
        assertThat(product1Items).hasSize(1);
        assertThat(product1Items.get(0).getTenantId()).isEqualTo(1);
    }

    @Test
    public void testTenantIsolationWithNullTenant() {
        // Test behavior when no tenant is set
        TenantContextHolder.clear();
        
        // Try to save an item without tenant context
        InventoryItem item = createTestItem(1, "Product1", "AVAILABLE");
        InventoryItem savedItem = inventoryItemRepository.save(item);
        
        // The item should still be saved, but tenant filtering won't work
        assertThat(savedItem.getItemId()).isNotNull();
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