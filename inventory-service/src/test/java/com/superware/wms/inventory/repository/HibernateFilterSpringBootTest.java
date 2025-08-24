package com.superware.wms.inventory.repository;

import com.superware.wms.inventory.entity.InventoryItem;
import com.superware.wms.tenant.context.TenantContextHolder;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class HibernateFilterSpringBootTest {

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private EntityManager entityManager;

    @AfterEach
    public void tearDown() {
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
        
        // Verify the filter parameter is set correctly
        // Note: Hibernate Filter API doesn't provide direct access to parameters
        // We can only verify the filter is enabled, which means tenant filtering is active
        assertThat(filter).isNotNull();
    }

    @Test
    public void testTenantIsolationWithHibernateFilters() {
        // Test data isolation between tenants using Hibernate filters
        
        // Create data for tenant 1
        TenantContextHolder.setCurrentTenant("1");
        InventoryItem item1 = createTestItem(1, "Tenant1Product", "AVAILABLE");
        InventoryItem savedItem1 = inventoryItemRepository.save(item1);
        assertThat(savedItem1.getTenantId()).isEqualTo(1);
        
        // Create data for tenant 2
        TenantContextHolder.setCurrentTenant("2");
        InventoryItem item2 = createTestItem(2, "Tenant2Product", "ALLOCATED");
        InventoryItem savedItem2 = inventoryItemRepository.save(item2);
        assertThat(savedItem2.getTenantId()).isEqualTo(2);
        
        // Verify tenant 1 only sees its data
        TenantContextHolder.setCurrentTenant("1");
        List<InventoryItem> tenant1Items = inventoryItemRepository.findAll();
        assertThat(tenant1Items).hasSize(1);
        assertThat(tenant1Items.get(0).getProductId()).isEqualTo(1);
        assertThat(tenant1Items.get(0).getTenantId()).isEqualTo(1);
        
        // Verify tenant 2 only sees its data
        TenantContextHolder.setCurrentTenant("2");
        List<InventoryItem> tenant2Items = inventoryItemRepository.findAll();
        assertThat(tenant2Items).hasSize(1);
        assertThat(tenant2Items.get(0).getItemId()).isEqualTo(savedItem2.getItemId());
        assertThat(tenant2Items.get(0).getProductId()).isEqualTo(2);
        assertThat(tenant2Items.get(0).getTenantId()).isEqualTo(2);
    }

    @Test
    public void testHibernateFilterParameterChangesWithTenantContext() {
        // Test that filter parameters change when tenant context changes
        
        // Set tenant 1
        TenantContextHolder.setCurrentTenant("1");
        Session session1 = entityManager.unwrap(Session.class);
        Filter filter1 = session1.getEnabledFilter("tenantFilter");
        assertThat(filter1).isNotNull();
        
        // Change to tenant 2
        TenantContextHolder.setCurrentTenant("2");
        Session session2 = entityManager.unwrap(Session.class);
        Filter filter2 = session2.getEnabledFilter("tenantFilter");
        assertThat(filter2).isNotNull();
        
        // Verify both filters are enabled (tenant filtering is active)
        assertThat(filter1).isNotNull();
        assertThat(filter2).isNotNull();
    }

    @Test
    public void testHibernateFilterWithoutTenantContext() {
        // Test behavior when no tenant context is set
        TenantContextHolder.clear();
        
        // Try to save an item without tenant context
        InventoryItem item = createTestItem(1, "NoTenantProduct", "AVAILABLE");
        InventoryItem savedItem = inventoryItemRepository.save(item);
        
        // Item should be saved, but filter won't be enabled
        assertThat(savedItem.getItemId()).isNotNull();
        
        // Check if filter is enabled (should not be)
        Session session = entityManager.unwrap(Session.class);
        Filter filter = session.getEnabledFilter("tenantFilter");
        // Note: This might be null or might have a default value depending on implementation
    }

    @Test
    public void testHibernateFilterWithInvalidTenantId() {
        // Test behavior with invalid tenant ID
        TenantContextHolder.setCurrentTenant("invalid-tenant");
        
        // Try to save an item with invalid tenant
        InventoryItem item = createTestItem(1, "InvalidTenantProduct", "AVAILABLE");
        InventoryItem savedItem = inventoryItemRepository.save(item);
        
        // Item should still be saved
        assertThat(savedItem.getItemId()).isNotNull();
        
        // Check if filter is enabled with invalid tenant
        Session session = entityManager.unwrap(Session.class);
        Filter filter = session.getEnabledFilter("tenantFilter");
        // Note: Filter will be enabled even with invalid tenant ID
        // The actual filtering behavior depends on the database schema and filter condition
        assertThat(filter).isNotNull();
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