package com.superware.wms.inventory.repository;

import com.superware.wms.inventory.entity.InventoryCountDetail;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for InventoryCountDetail entities.
 */
@Repository
public interface InventoryCountDetailRepository extends InventoryRepository<InventoryCountDetail, Integer> {
    
    @Query("SELECT icd FROM InventoryCountDetail icd WHERE icd.countId = :countId")
    List<InventoryCountDetail> findByCountId(@Param("countId") Integer countId);
}