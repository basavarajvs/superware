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
public class TenantAwareRepositorySimpleTest {

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
    public void testBasicRepositoryInjection() {
        // First, verify the repository is properly injected
        assertThat(inventoryItemRepository).isNotNull();
        
        // Verify it implements the TenantAwareRepository interface
        assertThat(TenantAwareRepository.class.isAssignableFrom(inventoryItemRepository.getClass())).isTrue();
        
        System.out.println("Repository class: " + inventoryItemRepository.getClass().getName());
        System.out.println("Repository interfaces: " + java.util.Arrays.toString(inventoryItemRepository.getClass().getInterfaces()));
    }

    @Test
    public void testBasicSaveAndFind() {
        // Test basic save and find functionality
        TenantContextHolder.setCurrentTenant("1");
        
        InventoryItem item = createTestItem(1, "TestProduct", "AVAILABLE");
        InventoryItem savedItem = inventoryItemRepository.save(item);
        
        assertThat(savedItem.getItemId()).isNotNull();
        assertThat(savedItem.getTenantId()).isEqualTo(1);
        
        // Test findById
        Optional<InventoryItem> foundItem = inventoryItemRepository.findById(savedItem.getItemId());
        assertThat(foundItem).isPresent();
        assertThat(foundItem.get().getTenantId()).isEqualTo(1);
    }

    @Test
    public void testTenantContextSwitching() {
        // Test switching between tenants
        assertThat(inventoryItemRepository).isNotNull();
        
        // Create item for tenant 1
        TenantContextHolder.setCurrentTenant("1");
        InventoryItem item1 = createTestItem(1, "Tenant1Product", "AVAILABLE");
        InventoryItem savedItem1 = inventoryItemRepository.save(item1);
        assertThat(savedItem1.getTenantId()).isEqualTo(1);
        
        // Create item for tenant 2
        TenantContextHolder.setCurrentTenant("2");
        InventoryItem item2 = createTestItem(2, "Tenant2Product", "AVAILABLE");
        InventoryItem savedItem2 = inventoryItemRepository.save(item2);
        assertThat(savedItem2.getTenantId()).isEqualTo(2);
        
        // Verify tenant isolation
        TenantContextHolder.setCurrentTenant("1");
        List<InventoryItem> tenant1Items = inventoryItemRepository.findAll();
        assertThat(tenant1Items).hasSize(1);
        assertThat(tenant1Items.get(0).getTenantId()).isEqualTo(1);
        
        TenantContextHolder.setCurrentTenant("2");
        List<InventoryItem> tenant2Items = inventoryItemRepository.findAll();
        assertThat(tenant2Items).hasSize(1);
        assertThat(tenant2Items.get(0).getTenantId()).isEqualTo(2);
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
