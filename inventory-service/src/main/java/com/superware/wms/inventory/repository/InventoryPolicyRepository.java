package com.superware.wms.inventory.repository;

import com.superware.wms.inventory.entity.InventoryPolicy;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for InventoryPolicy entities.
 */
@Repository
public interface InventoryPolicyRepository extends InventoryRepository<InventoryPolicy, Integer> {
    
    @Query("SELECT ip FROM InventoryPolicy ip WHERE ip.productId = :productId")
    List<InventoryPolicy> findByProductId(@Param("productId") Integer productId);
    
    @Query("SELECT ip FROM InventoryPolicy ip WHERE ip.facilityId = :facilityId")
    List<InventoryPolicy> findByFacilityId(@Param("facilityId") Integer facilityId);
}