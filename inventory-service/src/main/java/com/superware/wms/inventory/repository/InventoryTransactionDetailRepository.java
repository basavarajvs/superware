package com.superware.wms.inventory.repository;

import com.superware.wms.inventory.entity.InventoryTransactionDetail;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for InventoryTransactionDetail entities.
 */
@Repository
public interface InventoryTransactionDetailRepository extends InventoryRepository<InventoryTransactionDetail, Integer> {
    
    @Query("SELECT itd FROM InventoryTransactionDetail itd WHERE itd.transactionId = :transactionId")
    List<InventoryTransactionDetail> findByTransactionId(@Param("transactionId") Integer transactionId);
}