package com.superware.wms.inventory.repository;

import com.superware.wms.inventory.entity.InventoryAdjustment;
import org.springframework.stereotype.Repository;

/**
 * Repository for InventoryAdjustment entities.
 */
@Repository
public interface InventoryAdjustmentRepository extends InventoryRepository<InventoryAdjustment, Integer> {
}