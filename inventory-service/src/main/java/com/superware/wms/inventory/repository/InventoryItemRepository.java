package com.superware.wms.inventory.repository;

import com.superware.wms.inventory.entity.InventoryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for InventoryItem entities with automatic tenant filtering.
 * Extends TenantAwareRepository for multi-tenancy support.
 */
@Repository
public interface InventoryItemRepository extends TenantAwareRepository<InventoryItem, Integer> {
    
    List<InventoryItem> findByProductId(Integer productId);
    
    List<InventoryItem> findByStatus(String status);
    
    List<InventoryItem> findByQuantityOnHandGreaterThan(BigDecimal quantity);
    
    Optional<InventoryItem> findById(Integer id);
}