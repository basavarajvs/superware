package com.superware.wms.inventory.service;

import com.superware.wms.inventory.entity.InventoryTransaction;
import com.superware.wms.inventory.entity.InventoryTransactionDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for managing inventory transactions.
 */
public interface InventoryTransactionService {
    
    /**
     * Get all inventory transactions with pagination.
     *
     * @param pageable pagination information
     * @return page of inventory transactions
     */
    Page<InventoryTransaction> getAllTransactions(Pageable pageable);
    
    /**
     * Get an inventory transaction by ID.
     *
     * @param id the ID of the inventory transaction
     * @return the inventory transaction
     * @throws com.superware.wms.inventory.exception.ResourceNotFoundException if the inventory transaction is not found
     */
    InventoryTransaction getTransactionById(Integer id);
    
    /**
     * Create a new inventory transaction.
     *
     * @param transaction the inventory transaction to create
     * @return the created inventory transaction
     */
    InventoryTransaction createTransaction(InventoryTransaction transaction);
    
    /**
     * Update an existing inventory transaction.
     *
     * @param id the ID of the inventory transaction to update
     * @param transactionDetails the updated inventory transaction data
     * @return the updated inventory transaction
     * @throws com.superware.wms.inventory.exception.ResourceNotFoundException if the inventory transaction is not found
     */
    InventoryTransaction updateTransaction(Integer id, InventoryTransaction transactionDetails);
    
    /**
     * Delete an inventory transaction.
     *
     * @param id the ID of the inventory transaction to delete
     * @throws com.superware.wms.inventory.exception.ResourceNotFoundException if the inventory transaction is not found
     */
    void deleteTransaction(Integer id);
    
    /**
     * Get all inventory transaction details for a specific transaction.
     *
     * @param transactionId the ID of the transaction
     * @return list of inventory transaction details
     */
    List<InventoryTransactionDetail> getTransactionDetailsByTransactionId(Integer transactionId);
    
    /**
     * Record a stock receipt transaction.
     *
     * @param itemId the ID of the inventory item
     * @param quantity the quantity received
     * @param fromLocationId the ID of the source location
     * @param toLocationId the ID of the destination location
     * @param userId the ID of the user performing the transaction
     * @return the created inventory transaction
     * @throws com.superware.wms.inventory.exception.ResourceNotFoundException if the inventory item is not found
     */
    InventoryTransaction recordReceipt(Integer itemId, java.math.BigDecimal quantity, Integer fromLocationId, Integer toLocationId, Integer userId);
    
    /**
     * Record a stock issue transaction.
     *
     * @param itemId the ID of the inventory item
     * @param quantity the quantity issued
     * @param fromLocationId the ID of the source location
     * @param toLocationId the ID of the destination location
     * @param userId the ID of the user performing the transaction
     * @return the created inventory transaction
     * @throws com.superware.wms.inventory.exception.ResourceNotFoundException if the inventory item is not found
     * @throws com.superware.wms.inventory.exception.InsufficientStockException if there is insufficient stock
     */
    InventoryTransaction recordIssue(Integer itemId, java.math.BigDecimal quantity, Integer fromLocationId, Integer toLocationId, Integer userId);
    
    /**
     * Record a stock transfer transaction.
     *
     * @param itemId the ID of the inventory item
     * @param quantity the quantity transferred
     * @param fromLocationId the ID of the source location
     * @param toLocationId the ID of the destination location
     * @param userId the ID of the user performing the transaction
     * @return the created inventory transaction
     * @throws com.superware.wms.inventory.exception.ResourceNotFoundException if the inventory item is not found
     * @throws com.superware.wms.inventory.exception.InsufficientStockException if there is insufficient stock
     */
    InventoryTransaction recordTransfer(Integer itemId, java.math.BigDecimal quantity, Integer fromLocationId, Integer toLocationId, Integer userId);
}