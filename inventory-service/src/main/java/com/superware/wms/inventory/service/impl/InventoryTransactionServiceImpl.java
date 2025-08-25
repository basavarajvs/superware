package com.superware.wms.inventory.service.impl;

import com.superware.wms.inventory.entity.InventoryItem;
import com.superware.wms.inventory.entity.InventoryTransaction;
import com.superware.wms.inventory.entity.InventoryTransactionDetail;
import com.superware.wms.inventory.exception.InsufficientStockException;
import com.superware.wms.inventory.exception.ResourceNotFoundException;
import com.superware.wms.inventory.repository.InventoryItemRepository;
import com.superware.wms.inventory.repository.InventoryTransactionDetailRepository;
import com.superware.wms.inventory.repository.InventoryTransactionRepository;
import com.superware.wms.inventory.service.InventoryItemService;
import com.superware.wms.inventory.service.InventoryTransactionService;
import com.superware.wms.tenant.context.TenantContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of the InventoryTransactionService interface.
 */
@Service
@Transactional
public class InventoryTransactionServiceImpl implements InventoryTransactionService {

    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final InventoryTransactionDetailRepository inventoryTransactionDetailRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryItemService inventoryItemService;

    @Autowired
    public InventoryTransactionServiceImpl(
            InventoryTransactionRepository inventoryTransactionRepository,
            InventoryTransactionDetailRepository inventoryTransactionDetailRepository,
            InventoryItemRepository inventoryItemRepository,
            InventoryItemService inventoryItemService) {
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.inventoryTransactionDetailRepository = inventoryTransactionDetailRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryItemService = inventoryItemService;
    }

    @Override
    public Page<InventoryTransaction> getAllTransactions(Pageable pageable) {
        return inventoryTransactionRepository.findAll(pageable);
    }

    @Override
    public InventoryTransaction getTransactionById(Integer id) {
        return inventoryTransactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryTransaction", "id", id));
    }

    @Override
    public InventoryTransaction createTransaction(InventoryTransaction transaction) {
        transaction.setTenantId(Integer.valueOf(TenantContextHolder.getCurrentTenant()));
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        transaction.setCreatedBy(getCurrentUserId());
        return inventoryTransactionRepository.save(transaction);
    }

    @Override
    public InventoryTransaction updateTransaction(Integer id, InventoryTransaction transactionDetails) {
        InventoryTransaction transaction = getTransactionById(id);
        transaction.setTransactionType(transactionDetails.getTransactionType());
        transaction.setFromLocationId(transactionDetails.getFromLocationId());
        transaction.setToLocationId(transactionDetails.getToLocationId());
        transaction.setStatus(transactionDetails.getStatus());
        transaction.setNotes(transactionDetails.getNotes());
        transaction.setUpdatedAt(LocalDateTime.now());
        transaction.setUpdatedBy(getCurrentUserId());
        return inventoryTransactionRepository.save(transaction);
    }

    @Override
    public void deleteTransaction(Integer id) {
        InventoryTransaction transaction = getTransactionById(id);
        transaction.setIsDeleted(true);
        transaction.setUpdatedAt(LocalDateTime.now());
        transaction.setUpdatedBy(getCurrentUserId());
        inventoryTransactionRepository.save(transaction);
    }

    @Override
    public List<InventoryTransactionDetail> getTransactionDetailsByTransactionId(Integer transactionId) {
        return inventoryTransactionDetailRepository.findByTransactionId(transactionId);
    }

    @Override
    public InventoryTransaction recordReceipt(Integer itemId, BigDecimal quantity, Integer fromLocationId, Integer toLocationId, Integer userId) {
        // Get the inventory item
        InventoryItem item = inventoryItemService.getItemById(itemId);
        
        // Create the transaction record
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setTransactionType("RECEIPT");
        transaction.setFromLocationId(fromLocationId);
        transaction.setToLocationId(toLocationId);
        transaction.setStatus("COMPLETED");
        transaction.setCreatedBy(userId);
        transaction.setUpdatedBy(userId);
        
        // Save the transaction first to get its ID
        transaction = createTransaction(transaction);
        
        // Create the transaction detail
        InventoryTransactionDetail detail = new InventoryTransactionDetail();
        detail.setTransactionId(transaction.getId());
        detail.setItemId(itemId);
        detail.setQuantity(quantity);
        detail.setFromLocationId(fromLocationId);
        detail.setToLocationId(toLocationId);
        detail.setLotNumber(item.getLotNumber());
        detail.setCreatedBy(userId);
        detail.setUpdatedBy(userId);
        
        inventoryTransactionDetailRepository.save(detail);
        
        // Update the inventory item's quantity
        item.setQuantityOnHand(item.getQuantityOnHand().add(quantity));
        item.setUpdatedAt(LocalDateTime.now());
        item.setUpdatedBy(userId);
        inventoryItemRepository.save(item);
        
        return transaction;
    }

    @Override
    public InventoryTransaction recordIssue(Integer itemId, BigDecimal quantity, Integer fromLocationId, Integer toLocationId, Integer userId) {
        // Get the inventory item
        InventoryItem item = inventoryItemService.getItemById(itemId);
        
        // Validate that we have sufficient stock
        if (item.getQuantityOnHand().compareTo(quantity) < 0) {
            throw new InsufficientStockException(
                "InventoryItem", 
                itemId, 
                "issue stock", 
                item.getQuantityOnHand().toString(), 
                quantity.toString()
            );
        }
        
        // Create the transaction record
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setTransactionType("ISSUE");
        transaction.setFromLocationId(fromLocationId);
        transaction.setToLocationId(toLocationId);
        transaction.setStatus("COMPLETED");
        transaction.setCreatedBy(userId);
        transaction.setUpdatedBy(userId);
        
        // Save the transaction first to get its ID
        transaction = createTransaction(transaction);
        
        // Create the transaction detail
        InventoryTransactionDetail detail = new InventoryTransactionDetail();
        detail.setTransactionId(transaction.getId());
        detail.setItemId(itemId);
        detail.setQuantity(quantity);
        detail.setFromLocationId(fromLocationId);
        detail.setToLocationId(toLocationId);
        detail.setLotNumber(item.getLotNumber());
        detail.setCreatedBy(userId);
        detail.setUpdatedBy(userId);
        
        inventoryTransactionDetailRepository.save(detail);
        
        // Update the inventory item's quantity
        item.setQuantityOnHand(item.getQuantityOnHand().subtract(quantity));
        item.setUpdatedAt(LocalDateTime.now());
        item.setUpdatedBy(userId);
        inventoryItemRepository.save(item);
        
        return transaction;
    }

    @Override
    public InventoryTransaction recordTransfer(Integer itemId, BigDecimal quantity, Integer fromLocationId, Integer toLocationId, Integer userId) {
        // Get the inventory item
        InventoryItem item = inventoryItemService.getItemById(itemId);
        
        // Validate that we have sufficient stock
        if (item.getQuantityOnHand().compareTo(quantity) < 0) {
            throw new InsufficientStockException(
                "InventoryItem", 
                itemId, 
                "transfer stock", 
                item.getQuantityOnHand().toString(), 
                quantity.toString()
            );
        }
        
        // Create the transaction record
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setTransactionType("TRANSFER");
        transaction.setFromLocationId(fromLocationId);
        transaction.setToLocationId(toLocationId);
        transaction.setStatus("COMPLETED");
        transaction.setCreatedBy(userId);
        transaction.setUpdatedBy(userId);
        
        // Save the transaction first to get its ID
        transaction = createTransaction(transaction);
        
        // Create the transaction detail
        InventoryTransactionDetail detail = new InventoryTransactionDetail();
        detail.setTransactionId(transaction.getId());
        detail.setItemId(itemId);
        detail.setQuantity(quantity);
        detail.setFromLocationId(fromLocationId);
        detail.setToLocationId(toLocationId);
        detail.setLotNumber(item.getLotNumber());
        detail.setCreatedBy(userId);
        detail.setUpdatedBy(userId);
        
        inventoryTransactionDetailRepository.save(detail);
        
        // Note: In a real transfer, we would also update the location of the item
        // For now, we're just updating the quantity (assuming it's a logical transfer)
        
        return transaction;
    }

    private Integer getCurrentUserId() {
        // In a real implementation, this would come from the security context
        return 1;
    }
}