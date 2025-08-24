package com.superware.wms.inventory.repository;

import com.superware.wms.inventory.entity.InventoryCountDetail;
import org.springframework.stereotype.Repository;

/**
 * Repository for InventoryCountDetail entities.
 */
@Repository
public interface InventoryCountDetailRepository extends InventoryRepository<InventoryCountDetail, Integer> {
}