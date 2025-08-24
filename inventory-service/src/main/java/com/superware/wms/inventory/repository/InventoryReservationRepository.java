package com.superware.wms.inventory.repository;

import com.superware.wms.inventory.entity.InventoryReservation;
import org.springframework.stereotype.Repository;

/**
 * Repository for InventoryReservation entities.
 */
@Repository
public interface InventoryReservationRepository extends InventoryRepository<InventoryReservation, Integer> {
}