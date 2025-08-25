package com.superware.wms.inventory.repository;

import com.superware.wms.inventory.entity.InventoryAdjustmentDetail;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for InventoryAdjustmentDetail entities.
 */
@Repository
public interface InventoryAdjustmentDetailRepository extends InventoryRepository<InventoryAdjustmentDetail, Integer> {
    
    @Query("SELECT iad FROM InventoryAdjustmentDetail iad WHERE iad.adjustmentId = :adjustmentId")
    List<InventoryAdjustmentDetail> findByAdjustmentId(@Param("adjustmentId") Integer adjustmentId);
}