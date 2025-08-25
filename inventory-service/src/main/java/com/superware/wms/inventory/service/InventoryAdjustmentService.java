package com.superware.wms.inventory.service;

import com.superware.wms.inventory.entity.InventoryAdjustment;
import com.superware.wms.inventory.entity.InventoryAdjustmentDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for managing inventory adjustments.
 */
public interface InventoryAdjustmentService {
    
    /**
     * Get all inventory adjustments with pagination.
     *
     * @param pageable pagination information
     * @return page of inventory adjustments
     */
    Page<InventoryAdjustment> getAllAdjustments(Pageable pageable);
    
    /**
     * Get an inventory adjustment by ID.
     *
     * @param id the ID of the inventory adjustment
     * @return the inventory adjustment
     * @throws com.superware.wms.inventory.exception.ResourceNotFoundException if the inventory adjustment is not found
     */
    InventoryAdjustment getAdjustmentById(Integer id);
    
    /**
     * Create a new inventory adjustment.
     *
     * @param adjustment the inventory adjustment to create
     * @return the created inventory adjustment
     */
    InventoryAdjustment createAdjustment(InventoryAdjustment adjustment);
    
    /**
     * Update an existing inventory adjustment.
     *
     * @param id the ID of the inventory adjustment to update
     * @param adjustmentDetails the updated inventory adjustment data
     * @return the updated inventory adjustment
     * @throws com.superware.wms.inventory.exception.ResourceNotFoundException if the inventory adjustment is not found
     */
    InventoryAdjustment updateAdjustment(Integer id, InventoryAdjustment adjustmentDetails);
    
    /**
     * Delete an inventory adjustment.
     *
     * @param id the ID of the inventory adjustment to delete
     * @throws com.superware.wms.inventory.exception.ResourceNotFoundException if the inventory adjustment is not found
     */
    void deleteAdjustment(Integer id);
    
    /**
     * Get all inventory adjustment details for a specific adjustment.
     *
     * @param adjustmentId the ID of the adjustment
     * @return list of inventory adjustment details
     */
    List<InventoryAdjustmentDetail> getAdjustmentDetailsByAdjustmentId(Integer adjustmentId);
    
    /**
     * Adjust stock for an inventory item.
     *
     * @param itemId the ID of the inventory item
     * @param quantity the quantity to adjust (positive for increase, negative for decrease)
     * @param reason the reason for the adjustment
     * @param userId the ID of the user performing the adjustment
     * @return the created inventory adjustment
     * @throws com.superware.wms.inventory.exception.ResourceNotFoundException if the inventory item is not found
     * @throws com.superware.wms.inventory.exception.InsufficientStockException if there is insufficient stock for a negative adjustment
     */
    InventoryAdjustment adjustStock(Integer itemId, java.math.BigDecimal quantity, String reason, Integer userId);
}