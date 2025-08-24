package com.superware.wms.inventory.repository;

import com.superware.wms.inventory.entity.InventoryAllocation;
import org.springframework.stereotype.Repository;

/**
 * Repository for InventoryAllocation entities.
 */
@Repository
public interface InventoryAllocationRepository extends InventoryRepository<InventoryAllocation, Integer> {
}