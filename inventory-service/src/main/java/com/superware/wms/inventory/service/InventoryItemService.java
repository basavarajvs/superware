package com.superware.wms.inventory.service;

import com.superware.wms.inventory.entity.InventoryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for managing inventory items.
 */
public interface InventoryItemService {
    
    /**
     * Get all inventory items with pagination.
     *
     * @param pageable pagination information
     * @return page of inventory items
     */
    Page<InventoryItem> getAllItems(Pageable pageable);
    
    /**
     * Get an inventory item by ID.
     *
     * @param id the ID of the inventory item
     * @return the inventory item
     * @throws com.superware.wms.inventory.exception.ResourceNotFoundException if the inventory item is not found
     */
    InventoryItem getItemById(Integer id);
    
    /**
     * Create a new inventory item.
     *
     * @param item the inventory item to create
     * @return the created inventory item
     */
    InventoryItem createItem(InventoryItem item);
    
    /**
     * Update an existing inventory item.
     *
     * @param id the ID of the inventory item to update
     * @param itemDetails the updated inventory item data
     * @return the updated inventory item
     * @throws com.superware.wms.inventory.exception.ResourceNotFoundException if the inventory item is not found
     */
    InventoryItem updateItem(Integer id, InventoryItem itemDetails);
    
    /**
     * Delete an inventory item.
     *
     * @param id the ID of the inventory item to delete
     * @throws com.superware.wms.inventory.exception.ResourceNotFoundException if the inventory item is not found
     */
    void deleteItem(Integer id);
    
    /**
     * Get all inventory items for a specific product.
     *
     * @param productId the ID of the product
     * @return list of inventory items for the product
     */
    List<InventoryItem> getItemsByProductId(Integer productId);
    
    /**
     * Get all inventory items with a specific status.
     *
     * @param status the status to filter by (e.g., AVAILABLE, ALLOCATED, QUARANTINED)
     * @return list of inventory items with the specified status
     */
    List<InventoryItem> getItemsByStatus(String status);
    
    /**
     * Get all inventory items with quantity greater than the specified value.
     *
     * @param quantity the minimum quantity threshold
     * @return list of inventory items with quantity greater than the threshold
     */
    List<InventoryItem> getItemsByQuantityOnHandGreaterThan(BigDecimal quantity);
}
