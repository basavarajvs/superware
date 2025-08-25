package com.superware.wms.inventory.repository;

import com.superware.wms.inventory.entity.InventoryReservationDetail;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for InventoryReservationDetail entities.
 */
@Repository
public interface InventoryReservationDetailRepository extends InventoryRepository<InventoryReservationDetail, Integer> {
    
    @Query("SELECT ird FROM InventoryReservationDetail ird WHERE ird.reservationId = :reservationId")
    List<InventoryReservationDetail> findByReservationId(@Param("reservationId") Integer reservationId);
}