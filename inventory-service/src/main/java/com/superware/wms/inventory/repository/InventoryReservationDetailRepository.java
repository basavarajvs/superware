package com.superware.wms.inventory.repository;

import com.superware.wms.inventory.entity.InventoryReservationDetail;
import org.springframework.stereotype.Repository;

/**
 * Repository for InventoryReservationDetail entities.
 */
@Repository
public interface InventoryReservationDetailRepository extends InventoryRepository<InventoryReservationDetail, Integer> {
}