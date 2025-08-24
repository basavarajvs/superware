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
public class TenantAwareRepositoryIntegrationTest {

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @BeforeEach
    public void setUp() {
        TenantContextHolder.clear();
    }

    @AfterEach
    public void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    public void testCompleteMultiTenancyWorkflow() {
        // Test the complete multi-tenancy workflow from creation to retrieval
        
        // Step 1: Create data for tenant 1
        TenantContextHolder.setCurrentTenant("1");
        InventoryItem tenant1Item1 = createTestItem(1, "Tenant1Product1", "AVAILABLE");
        InventoryItem tenant1Item2 = createTestItem(2, "Tenant1Product2", "ALLOCATED");
        
        InventoryItem savedTenant1Item1 = inventoryItemRepository.save(tenant1Item1);
        InventoryItem savedTenant1Item2 = inventoryItemRepository.save(tenant1Item2);
        
        // Verify tenant IDs are set correctly
        assertThat(savedTenant1Item1.getTenantId()).isEqualTo(1);
        assertThat(savedTenant1Item2.getTenantId()).isEqualTo(1);
        
        // Step 2: Create data for tenant 2
        TenantContextHolder.setCurrentTenant("2");
        InventoryItem tenant2Item1 = createTestItem(3, "Tenant2Product1", "AVAILABLE");
        InventoryItem tenant2Item2 = createTestItem(4, "Tenant2Product2", "RESERVED");
        
        InventoryItem savedTenant2Item1 = inventoryItemRepository.save(tenant2Item1);
        InventoryItem savedTenant2Item2 = inventoryItemRepository.save(tenant2Item2);
        
        // Verify tenant IDs are set correctly
        assertThat(savedTenant2Item1.getTenantId()).isEqualTo(2);
        assertThat(savedTenant2Item2.getTenantId()).isEqualTo(2);
        
        // Step 3: Verify tenant 1 only sees its own data
        TenantContextHolder.setCurrentTenant("1");
        List<InventoryItem> tenant1Items = inventoryItemRepository.findAll();
        assertThat(tenant1Items).hasSize(2);
        
        // Verify all items belong to tenant 1
        assertThat(tenant1Items).allMatch(item -> item.getTenantId().equals(1));
        
        // Verify specific items are present
        assertThat(tenant1Items).anyMatch(item -> item.getProductId().equals(1));
        assertThat(tenant1Items).anyMatch(item -> item.getProductId().equals(2));
        
        // Step 4: Verify tenant 2 only sees its own data
        TenantContextHolder.setCurrentTenant("2");
        List<InventoryItem> tenant2Items = inventoryItemRepository.findAll();
        assertThat(tenant2Items).hasSize(2);
        
        // Verify all items belong to tenant 2
        assertThat(tenant2Items).allMatch(item -> item.getTenantId().equals(2));
        
        // Verify specific items are present
        assertThat(tenant2Items).anyMatch(item -> item.getProductId().equals(3));
        assertThat(tenant2Items).anyMatch(item -> item.getProductId().equals(4));
        
        // Step 5: Verify cross-tenant isolation
        // Tenant 1 should not see tenant 2's items
        TenantContextHolder.setCurrentTenant("1");
        Optional<InventoryItem> crossTenantItem = inventoryItemRepository.findById(savedTenant2Item1.getItemId());
        assertThat(crossTenantItem).isNotPresent();
        
        // Tenant 2 should not see tenant 1's items
        TenantContextHolder.setCurrentTenant("2");
        Optional<InventoryItem> crossTenantItem2 = inventoryItemRepository.findById(savedTenant1Item1.getItemId());
        assertThat(crossTenantItem2).isNotPresent();
    }

    @Test
    public void testTenantContextPersistence() {
        // Test that tenant context persists across multiple operations
        
        TenantContextHolder.setCurrentTenant("1");
        
        // Create multiple items
        InventoryItem item1 = createTestItem(1, "Product1", "AVAILABLE");
        InventoryItem item2 = createTestItem(2, "Product2", "ALLOCATED");
        InventoryItem item3 = createTestItem(3, "Product3", "RESERVED");
        
        inventoryItemRepository.save(item1);
        inventoryItemRepository.save(item2);
        inventoryItemRepository.save(item3);
        
        // Verify all items have correct tenant ID
        List<InventoryItem> allItems = inventoryItemRepository.findAll();
        assertThat(allItems).hasSize(3);
        assertThat(allItems).allMatch(item -> item.getTenantId().equals(1));
        
        // Test findById operations
        Optional<InventoryItem> foundItem1 = inventoryItemRepository.findById(item1.getItemId());
        Optional<InventoryItem> foundItem2 = inventoryItemRepository.findById(item2.getItemId());
        Optional<InventoryItem> foundItem3 = inventoryItemRepository.findById(item3.getItemId());
        
        assertThat(foundItem1).isPresent();
        assertThat(foundItem2).isPresent();
        assertThat(foundItem3).isPresent();
        
        assertThat(foundItem1.get().getTenantId()).isEqualTo(1);
        assertThat(foundItem2.get().getTenantId()).isEqualTo(1);
        assertThat(foundItem3.get().getTenantId()).isEqualTo(1);
    }

    @Test
    public void testTenantContextSwitching() {
        // Test switching between tenants in the same test method
        
        // Create data for tenant 1
        TenantContextHolder.setCurrentTenant("1");
        InventoryItem tenant1Item = createTestItem(1, "Tenant1Product", "AVAILABLE");
        InventoryItem savedTenant1Item = inventoryItemRepository.save(tenant1Item);
        
        // Switch to tenant 2
        TenantContextHolder.setCurrentTenant("2");
        InventoryItem tenant2Item = createTestItem(2, "Tenant2Product", "AVAILABLE");
        InventoryItem savedTenant2Item = inventoryItemRepository.save(tenant2Item);
        
        // Switch back to tenant 1 and verify data
        TenantContextHolder.setCurrentTenant("1");
        List<InventoryItem> tenant1Items = inventoryItemRepository.findAll();
        assertThat(tenant1Items).hasSize(1);
        assertThat(tenant1Items.get(0).getTenantId()).isEqualTo(1);
        assertThat(tenant1Items.get(0).getProductId()).isEqualTo(1);
        
        // Switch to tenant 2 and verify data
        TenantContextHolder.setCurrentTenant("2");
        List<InventoryItem> tenant2Items = inventoryItemRepository.findAll();
        assertThat(tenant2Items).hasSize(1);
        assertThat(tenant2Items.get(0).getTenantId()).isEqualTo(2);
        assertThat(tenant2Items.get(0).getProductId()).isEqualTo(2);
    }

    @Test
    public void testCustomQueryMethodsWithTenantFiltering() {
        // Test that custom query methods respect tenant filtering
        
        // Create data for tenant 1
        TenantContextHolder.setCurrentTenant("1");
        InventoryItem item1 = createTestItem(1, "Product1", "AVAILABLE");
        InventoryItem item2 = createTestItem(2, "Product2", "AVAILABLE");
        InventoryItem item3 = createTestItem(3, "Product3", "ALLOCATED");
        
        inventoryItemRepository.save(item1);
        inventoryItemRepository.save(item2);
        inventoryItemRepository.save(item3);
        
        // Create data for tenant 2
        TenantContextHolder.setCurrentTenant("2");
        InventoryItem item4 = createTestItem(4, "Product4", "AVAILABLE");
        InventoryItem item5 = createTestItem(5, "Product5", "RESERVED");
        
        inventoryItemRepository.save(item4);
        inventoryItemRepository.save(item5);
        
        // Test custom queries for tenant 1
        TenantContextHolder.setCurrentTenant("1");
        
        List<InventoryItem> availableItems = inventoryItemRepository.findByStatus("AVAILABLE");
        assertThat(availableItems).hasSize(2);
        assertThat(availableItems).allMatch(item -> item.getTenantId().equals(1));
        
        List<InventoryItem> product1Items = inventoryItemRepository.findByProductId(1);
        assertThat(product1Items).hasSize(1);
        assertThat(product1Items.get(0).getTenantId()).isEqualTo(1);
        
        // Test custom queries for tenant 2
        TenantContextHolder.setCurrentTenant("2");
        
        List<InventoryItem> tenant2AvailableItems = inventoryItemRepository.findByStatus("AVAILABLE");
        assertThat(tenant2AvailableItems).hasSize(1);
        assertThat(tenant2AvailableItems).allMatch(item -> item.getTenantId().equals(2));
        
        List<InventoryItem> product4Items = inventoryItemRepository.findByProductId(4);
        assertThat(product4Items).hasSize(1);
        assertThat(product4Items.get(0).getTenantId()).isEqualTo(2);
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