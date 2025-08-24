package com.superware.wms.inventory.repository;

import com.superware.wms.inventory.entity.InventoryTransaction;
import org.springframework.stereotype.Repository;

/**
 * Repository for InventoryTransaction entities.
 */
@Repository
public interface InventoryTransactionRepository extends InventoryRepository<InventoryTransaction, Integer> {
}