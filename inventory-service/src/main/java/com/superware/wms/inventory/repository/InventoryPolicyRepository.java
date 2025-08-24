package com.superware.wms.inventory.repository;

import com.superware.wms.inventory.entity.InventoryPolicy;
import org.springframework.stereotype.Repository;

/**
 * Repository for InventoryPolicy entities.
 */
@Repository
public interface InventoryPolicyRepository extends InventoryRepository<InventoryPolicy, Integer> {
}