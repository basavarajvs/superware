package com.superware.wms.inventory.repository;

import com.superware.wms.inventory.entity.InventoryCount;
import org.springframework.stereotype.Repository;

/**
 * Repository for InventoryCount entities.
 */
@Repository
public interface InventoryCountRepository extends InventoryRepository<InventoryCount, Integer> {
}