package com.superware.wms.inventory.repository;

import com.superware.wms.inventory.entity.InventoryAdjustmentDetail;
import org.springframework.stereotype.Repository;

/**
 * Repository for InventoryAdjustmentDetail entities.
 */
@Repository
public interface InventoryAdjustmentDetailRepository extends InventoryRepository<InventoryAdjustmentDetail, Integer> {
}