package com.superware.wms.inventory.service.impl;

import com.superware.wms.inventory.entity.InventoryAdjustment;
import com.superware.wms.inventory.entity.InventoryAdjustmentDetail;
import com.superware.wms.inventory.entity.InventoryItem;
import com.superware.wms.inventory.exception.InsufficientStockException;
import com.superware.wms.inventory.exception.ResourceNotFoundException;
import com.superware.wms.inventory.repository.InventoryAdjustmentDetailRepository;
import com.superware.wms.inventory.repository.InventoryAdjustmentRepository;
import com.superware.wms.inventory.repository.InventoryItemRepository;
import com.superware.wms.inventory.service.InventoryAdjustmentService;
import com.superware.wms.inventory.service.InventoryItemService;
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
 * Implementation of the InventoryAdjustmentService interface.
 */
@Service
@Transactional
public class InventoryAdjustmentServiceImpl implements InventoryAdjustmentService {

    private final InventoryAdjustmentRepository inventoryAdjustmentRepository;
    private final InventoryAdjustmentDetailRepository inventoryAdjustmentDetailRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryItemService inventoryItemService;

    @Autowired
    public InventoryAdjustmentServiceImpl(
            InventoryAdjustmentRepository inventoryAdjustmentRepository,
            InventoryAdjustmentDetailRepository inventoryAdjustmentDetailRepository,
            InventoryItemRepository inventoryItemRepository,
            InventoryItemService inventoryItemService) {
        this.inventoryAdjustmentRepository = inventoryAdjustmentRepository;
        this.inventoryAdjustmentDetailRepository = inventoryAdjustmentDetailRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryItemService = inventoryItemService;
    }

    @Override
    public Page<InventoryAdjustment> getAllAdjustments(Pageable pageable) {
        return inventoryAdjustmentRepository.findAll(pageable);
    }

    @Override
    public InventoryAdjustment getAdjustmentById(Integer id) {
        return inventoryAdjustmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryAdjustment", "id", id));
    }

    @Override
    public InventoryAdjustment createAdjustment(InventoryAdjustment adjustment) {
        adjustment.setTenantId(Integer.valueOf(TenantContextHolder.getCurrentTenant()));
        adjustment.setCreatedAt(LocalDateTime.now());
        adjustment.setUpdatedAt(LocalDateTime.now());
        adjustment.setCreatedBy(getCurrentUserId());
        return inventoryAdjustmentRepository.save(adjustment);
    }

    @Override
    public InventoryAdjustment updateAdjustment(Integer id, InventoryAdjustment adjustmentDetails) {
        InventoryAdjustment adjustment = getAdjustmentById(id);
        adjustment.setAdjustmentType(adjustmentDetails.getAdjustmentType());
        adjustment.setReason(adjustmentDetails.getReason());
        adjustment.setStatus(adjustmentDetails.getStatus());
        adjustment.setNotes(adjustmentDetails.getNotes());
        adjustment.setUpdatedAt(LocalDateTime.now());
        adjustment.setUpdatedBy(getCurrentUserId());
        return inventoryAdjustmentRepository.save(adjustment);
    }

    @Override
    public void deleteAdjustment(Integer id) {
        InventoryAdjustment adjustment = getAdjustmentById(id);
        adjustment.setIsDeleted(true);
        adjustment.setUpdatedAt(LocalDateTime.now());
        adjustment.setUpdatedBy(getCurrentUserId());
        inventoryAdjustmentRepository.save(adjustment);
    }

    @Override
    public List<InventoryAdjustmentDetail> getAdjustmentDetailsByAdjustmentId(Integer adjustmentId) {
        return inventoryAdjustmentDetailRepository.findByAdjustmentId(adjustmentId);
    }

    @Override
    public InventoryAdjustment adjustStock(Integer itemId, BigDecimal quantity, String reason, Integer userId) {
        // Get the inventory item
        InventoryItem item = inventoryItemService.getItemById(itemId);
        
        // Validate that we have sufficient stock for negative adjustments
        if (quantity.compareTo(BigDecimal.ZERO) < 0 && 
            item.getQuantityOnHand().compareTo(quantity.abs()) < 0) {
            throw new InsufficientStockException(
                "InventoryItem", 
                itemId, 
                "decrease stock", 
                item.getQuantityOnHand().toString(), 
                quantity.abs().toString()
            );
        }
        
        // Create the adjustment record
        InventoryAdjustment adjustment = new InventoryAdjustment();
        adjustment.setAdjustmentType(quantity.compareTo(BigDecimal.ZERO) > 0 ? "INCREASE" : "DECREASE");
        adjustment.setReason(reason);
        adjustment.setStatus("APPROVED");
        adjustment.setCreatedBy(userId);
        adjustment.setUpdatedBy(userId);
        
        // Save the adjustment first to get its ID
        adjustment = createAdjustment(adjustment);
        
        // Create the adjustment detail
        InventoryAdjustmentDetail detail = new InventoryAdjustmentDetail();
        detail.setAdjustmentId(adjustment.getId());
        detail.setItemId(itemId);
        detail.setQuantityAdjusted(quantity);
        detail.setPreviousQuantity(item.getQuantityOnHand());
        detail.setNewQuantity(item.getQuantityOnHand().add(quantity));
        detail.setLotNumber(item.getLotNumber());
        detail.setReason(reason);
        detail.setCreatedBy(userId);
        detail.setUpdatedBy(userId);
        
        inventoryAdjustmentDetailRepository.save(detail);
        
        // Update the inventory item's quantity
        item.setQuantityOnHand(item.getQuantityOnHand().add(quantity));
        item.setUpdatedAt(LocalDateTime.now());
        item.setUpdatedBy(userId);
        inventoryItemRepository.save(item);
        
        return adjustment;
    }

    private Integer getCurrentUserId() {
        // In a real implementation, this would come from the security context
        return 1;
    }
}