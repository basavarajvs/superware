package com.superware.wms.inventory.service.impl;

import com.superware.wms.inventory.entity.InventoryCount;
import com.superware.wms.inventory.entity.InventoryCountDetail;
import com.superware.wms.inventory.entity.InventoryItem;
import com.superware.wms.inventory.exception.ResourceNotFoundException;
import com.superware.wms.inventory.repository.InventoryCountDetailRepository;
import com.superware.wms.inventory.repository.InventoryCountRepository;
import com.superware.wms.inventory.repository.InventoryItemRepository;
import com.superware.wms.inventory.service.InventoryAdjustmentService;
import com.superware.wms.inventory.service.InventoryCountService;
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
 * Implementation of the InventoryCountService interface.
 */
@Service
@Transactional
public class InventoryCountServiceImpl implements InventoryCountService {

    private final InventoryCountRepository inventoryCountRepository;
    private final InventoryCountDetailRepository inventoryCountDetailRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryItemService inventoryItemService;
    private final InventoryAdjustmentService inventoryAdjustmentService;

    @Autowired
    public InventoryCountServiceImpl(
            InventoryCountRepository inventoryCountRepository,
            InventoryCountDetailRepository inventoryCountDetailRepository,
            InventoryItemRepository inventoryItemRepository,
            InventoryItemService inventoryItemService,
            InventoryAdjustmentService inventoryAdjustmentService) {
        this.inventoryCountRepository = inventoryCountRepository;
        this.inventoryCountDetailRepository = inventoryCountDetailRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryItemService = inventoryItemService;
        this.inventoryAdjustmentService = inventoryAdjustmentService;
    }

    @Override
    public Page<InventoryCount> getAllCounts(Pageable pageable) {
        return inventoryCountRepository.findAll(pageable);
    }

    @Override
    public InventoryCount getCountById(Integer id) {
        return inventoryCountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryCount", "id", id));
    }

    @Override
    public InventoryCount createCount(InventoryCount count) {
        count.setTenantId(Integer.valueOf(TenantContextHolder.getCurrentTenant()));
        count.setCreatedAt(LocalDateTime.now());
        count.setUpdatedAt(LocalDateTime.now());
        count.setCreatedBy(getCurrentUserId());
        return inventoryCountRepository.save(count);
    }

    @Override
    public InventoryCount updateCount(Integer id, InventoryCount countDetails) {
        InventoryCount count = getCountById(id);
        count.setLocationId(countDetails.getLocationId());
        count.setStatus(countDetails.getStatus());
        count.setNotes(countDetails.getNotes());
        count.setUpdatedAt(LocalDateTime.now());
        count.setUpdatedBy(getCurrentUserId());
        return inventoryCountRepository.save(count);
    }

    @Override
    public void deleteCount(Integer id) {
        InventoryCount count = getCountById(id);
        count.setIsDeleted(true);
        count.setUpdatedAt(LocalDateTime.now());
        count.setUpdatedBy(getCurrentUserId());
        inventoryCountRepository.save(count);
    }

    @Override
    public List<InventoryCountDetail> getCountDetailsByCountId(Integer countId) {
        return inventoryCountDetailRepository.findByCountId(countId);
    }

    @Override
    public InventoryCount startCount(Integer locationId, Integer userId) {
        // Create the count record
        InventoryCount count = new InventoryCount();
        count.setLocationId(locationId);
        count.setStatus("IN_PROGRESS");
        count.setCreatedBy(userId);
        count.setUpdatedBy(userId);
        
        return createCount(count);
    }

    @Override
    public InventoryCountDetail addCountDetail(Integer countId, Integer itemId, BigDecimal countedQuantity, Integer userId) {
        // Verify the count exists
        getCountById(countId);
        
        // Get the inventory item
        InventoryItem item = inventoryItemService.getItemById(itemId);
        
        // Create the count detail
        InventoryCountDetail detail = new InventoryCountDetail();
        detail.setCountId(countId);
        detail.setItemId(itemId);
        detail.setSystemQuantity(item.getQuantityOnHand());
        detail.setCountedQuantity(countedQuantity);
        detail.setVariance(countedQuantity.subtract(item.getQuantityOnHand()));
        detail.setLotNumber(item.getLotNumber());
        detail.setCreatedBy(userId);
        detail.setUpdatedBy(userId);
        
        return inventoryCountDetailRepository.save(detail);
    }

    @Override
    public void completeCount(Integer countId, Integer userId) {
        // Get the count
        InventoryCount count = getCountById(countId);
        
        // Get all count details
        List<InventoryCountDetail> details = getCountDetailsByCountId(countId);
        
        // Process variances by creating adjustments
        for (InventoryCountDetail detail : details) {
            BigDecimal variance = detail.getVariance();
            if (variance.compareTo(BigDecimal.ZERO) != 0) {
                // Create an adjustment for the variance
                inventoryAdjustmentService.adjustStock(
                    detail.getItemId(), 
                    variance, 
                    "Cycle Count Variance", 
                    userId
                );
            }
            
            // Update the inventory item's last counted date
            InventoryItem item = inventoryItemService.getItemById(detail.getItemId());
            item.setLastCountedDate(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());
            item.setUpdatedBy(userId);
            inventoryItemRepository.save(item);
        }
        
        // Update the count status
        count.setStatus("COMPLETED");
        count.setUpdatedAt(LocalDateTime.now());
        count.setUpdatedBy(userId);
        inventoryCountRepository.save(count);
    }

    private Integer getCurrentUserId() {
        // In a real implementation, this would come from the security context
        return 1;
    }
}