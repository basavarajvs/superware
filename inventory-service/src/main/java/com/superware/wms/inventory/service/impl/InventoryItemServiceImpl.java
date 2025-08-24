package com.superware.wms.inventory.service.impl;

import com.superware.wms.inventory.entity.InventoryItem;
import com.superware.wms.inventory.exception.ResourceNotFoundException;
import com.superware.wms.inventory.repository.InventoryItemRepository;
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
 * Implementation of the InventoryItemService interface.
 */
@Service
@Transactional
public class InventoryItemServiceImpl implements InventoryItemService {

    private final InventoryItemRepository inventoryItemRepository;

    @Autowired
    public InventoryItemServiceImpl(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
    }

    @Override
    public Page<InventoryItem> getAllItems(Pageable pageable) {
        return inventoryItemRepository.findAll(pageable);
    }

    @Override
    public InventoryItem getItemById(Integer id) {
        return inventoryItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem", "id", id));
    }

    @Override
    public InventoryItem createItem(InventoryItem item) {
        item.setTenantId(Integer.valueOf(TenantContextHolder.getCurrentTenant()));
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        item.setCreatedBy(getCurrentUserId());
        return inventoryItemRepository.save(item);
    }

    @Override
    public InventoryItem updateItem(Integer id, InventoryItem itemDetails) {
        InventoryItem item = getItemById(id);
        item.setProductId(itemDetails.getProductId());
        item.setVariantId(itemDetails.getVariantId());
        item.setLotNumber(itemDetails.getLotNumber());
        item.setSerialNumber(itemDetails.getSerialNumber());
        item.setStatus(itemDetails.getStatus());
        item.setCondition(itemDetails.getCondition());
        item.setQuantityOnHand(itemDetails.getQuantityOnHand());
        item.setQuantityAllocated(itemDetails.getQuantityAllocated());
        item.setUnitOfMeasure(itemDetails.getUnitOfMeasure());
        item.setLocationId(itemDetails.getLocationId());
        item.setFacilityId(itemDetails.getFacilityId());
        item.setExpiryDate(itemDetails.getExpiryDate());
        item.setManufactureDate(itemDetails.getManufactureDate());
        item.setUnitCost(itemDetails.getUnitCost());
        item.setNotes(itemDetails.getNotes());
        item.setUpdatedAt(LocalDateTime.now());
        item.setUpdatedBy(getCurrentUserId());
        return inventoryItemRepository.save(item);
    }

    @Override
    public void deleteItem(Integer id) {
        InventoryItem item = getItemById(id);
        item.setIsDeleted(true);
        item.setUpdatedAt(LocalDateTime.now());
        item.setUpdatedBy(getCurrentUserId());
        inventoryItemRepository.save(item);
    }

    @Override
    public List<InventoryItem> getItemsByProductId(Integer productId) {
        return inventoryItemRepository.findByProductId(productId);
    }

    @Override
    public List<InventoryItem> getItemsByStatus(String status) {
        return inventoryItemRepository.findByStatus(status);
    }

    @Override
    public List<InventoryItem> getItemsByQuantityOnHandGreaterThan(BigDecimal quantity) {
        return inventoryItemRepository.findByQuantityOnHandGreaterThan(quantity);
    }

    private Integer getCurrentUserId() {
        // In a real implementation, this would come from the security context
        return 1;
    }
}