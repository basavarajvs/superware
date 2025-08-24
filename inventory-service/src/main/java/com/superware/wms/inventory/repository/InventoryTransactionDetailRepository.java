package com.superware.wms.inventory.repository;

import com.superware.wms.inventory.entity.InventoryTransactionDetail;
import org.springframework.stereotype.Repository;

/**
 * Repository for InventoryTransactionDetail entities.
 */
@Repository
public interface InventoryTransactionDetailRepository extends InventoryRepository<InventoryTransactionDetail, Integer> {
}