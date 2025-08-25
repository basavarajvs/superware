package com.superware.wms.inventory.service;

import com.superware.wms.inventory.entity.InventoryCount;
import com.superware.wms.inventory.entity.InventoryCountDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for managing inventory counts (cycle counts).
 */
public interface InventoryCountService {
    
    /**
     * Get all inventory counts with pagination.
     *
     * @param pageable pagination information
     * @return page of inventory counts
     */
    Page<InventoryCount> getAllCounts(Pageable pageable);
    
    /**
     * Get an inventory count by ID.
     *
     * @param id the ID of the inventory count
     * @return the inventory count
     * @throws com.superware.wms.inventory.exception.ResourceNotFoundException if the inventory count is not found
     */
    InventoryCount getCountById(Integer id);
    
    /**
     * Create a new inventory count.
     *
     * @param count the inventory count to create
     * @return the created inventory count
     */
    InventoryCount createCount(InventoryCount count);
    
    /**
     * Update an existing inventory count.
     *
     * @param id the ID of the inventory count to update
     * @param countDetails the updated inventory count data
     * @return the updated inventory count
     * @throws com.superware.wms.inventory.exception.ResourceNotFoundException if the inventory count is not found
     */
    InventoryCount updateCount(Integer id, InventoryCount countDetails);
    
    /**
     * Delete an inventory count.
     *
     * @param id the ID of the inventory count to delete
     * @throws com.superware.wms.inventory.exception.ResourceNotFoundException if the inventory count is not found
     */
    void deleteCount(Integer id);
    
    /**
     * Get all inventory count details for a specific count.
     *
     * @param countId the ID of the count
     * @return list of inventory count details
     */
    List<InventoryCountDetail> getCountDetailsByCountId(Integer countId);
    
    /**
     * Start a new inventory count.
     *
     * @param locationId the ID of the location to count
     * @param userId the ID of the user starting the count
     * @return the created inventory count
     */
    InventoryCount startCount(Integer locationId, Integer userId);
    
    /**
     * Add a count detail to an inventory count.
     *
     * @param countId the ID of the inventory count
     * @param itemId the ID of the inventory item
     * @param countedQuantity the quantity counted
     * @param userId the ID of the user performing the count
     * @return the created inventory count detail
     * @throws com.superware.wms.inventory.exception.ResourceNotFoundException if the inventory count or item is not found
     */
    InventoryCountDetail addCountDetail(Integer countId, Integer itemId, java.math.BigDecimal countedQuantity, Integer userId);
    
    /**
     * Complete an inventory count and process any variances.
     *
     * @param countId the ID of the inventory count to complete
     * @param userId the ID of the user completing the count
     * @throws com.superware.wms.inventory.exception.ResourceNotFoundException if the inventory count is not found
     */
    void completeCount(Integer countId, Integer userId);
}