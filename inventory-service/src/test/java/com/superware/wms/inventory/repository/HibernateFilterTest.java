package com.superware.wms.inventory.repository;

import com.superware.wms.inventory.config.TestRepositoryConfig;
import com.superware.wms.inventory.entity.InventoryItem;
import com.superware.wms.tenant.context.TenantContextHolder;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestRepositoryConfig.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class HibernateFilterTest {

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private EntityManager entityManager;

    @AfterEach
    public void tearDown() {
        TenantContextHolder.clear();
        TenantContextHolder.setCurrentTenant("1");
        inventoryItemRepository.deleteAll();
        TenantContextHolder.setCurrentTenant("2");
        inventoryItemRepository.deleteAll();
        TenantContextHolder.clear();
    }

    @Test
    public void testHibernateFilterIsEnabledDuringRepositoryOperations() {
        // Set tenant context
        TenantContextHolder.setCurrentTenant("1");

        // Create and save an item
        InventoryItem item = createTestItem(1, "Product1", "AVAILABLE");
        inventoryItemRepository.save(item);

        // Check if the tenant filter is enabled
        Session session = entityManager.unwrap(Session.class);
        Filter filter = session.getEnabledFilter("tenantFilter");
        assertThat(filter).isNotNull();
        // Note: We can't easily check the parameter value in this test setup
        // but we can verify the filter is enabled
    }

    @Test
    public void testTenantIsolationWithHibernateFilters() {
        // Test data isolation between tenants using Hibernate filters
        
        // Create data for tenant 1
        TenantContextHolder.setCurrentTenant("1");
        InventoryItem item1 = createTestItem(1, "Tenant1Product", "AVAILABLE");
        inventoryItemRepository.save(item1);
        
        // Create data for tenant 2
        TenantContextHolder.setCurrentTenant("2");
        InventoryItem item2 = createTestItem(2, "Tenant2Product", "ALLOCATED");
        InventoryItem savedItem2 = inventoryItemRepository.save(item2);
        
        // Verify tenant 1 only sees its data
        TenantContextHolder.setCurrentTenant("1");
        List<InventoryItem> tenant1Items = inventoryItemRepository.findAll();
        assertThat(tenant1Items).hasSize(1);
        assertThat(tenant1Items.get(0).getProductId()).isEqualTo(1);
        
        // Verify tenant 2 only sees its data
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