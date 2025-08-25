package com.superware.wms.inventory.service.impl;

import com.superware.wms.inventory.entity.InventoryItem;
import com.superware.wms.inventory.entity.InventoryReservation;
import com.superware.wms.inventory.entity.InventoryReservationDetail;
import com.superware.wms.inventory.exception.InsufficientStockException;
import com.superware.wms.inventory.exception.ResourceNotFoundException;
import com.superware.wms.inventory.repository.InventoryItemRepository;
import com.superware.wms.inventory.repository.InventoryReservationDetailRepository;
import com.superware.wms.inventory.repository.InventoryReservationRepository;
import com.superware.wms.inventory.service.InventoryItemService;
import com.superware.wms.inventory.service.InventoryReservationService;
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
 * Implementation of the InventoryReservationService interface.
 */
@Service
@Transactional
public class InventoryReservationServiceImpl implements InventoryReservationService {

    private final InventoryReservationRepository inventoryReservationRepository;
    private final InventoryReservationDetailRepository inventoryReservationDetailRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryItemService inventoryItemService;

    @Autowired
    public InventoryReservationServiceImpl(
            InventoryReservationRepository inventoryReservationRepository,
            InventoryReservationDetailRepository inventoryReservationDetailRepository,
            InventoryItemRepository inventoryItemRepository,
            InventoryItemService inventoryItemService) {
        this.inventoryReservationRepository = inventoryReservationRepository;
        this.inventoryReservationDetailRepository = inventoryReservationDetailRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryItemService = inventoryItemService;
    }

    @Override
    public Page<InventoryReservation> getAllReservations(Pageable pageable) {
        return inventoryReservationRepository.findAll(pageable);
    }

    @Override
    public InventoryReservation getReservationById(Integer id) {
        return inventoryReservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryReservation", "id", id));
    }

    @Override
    public InventoryReservation createReservation(InventoryReservation reservation) {
        reservation.setTenantId(Integer.valueOf(TenantContextHolder.getCurrentTenant()));
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setUpdatedAt(LocalDateTime.now());
        reservation.setCreatedBy(getCurrentUserId());
        return inventoryReservationRepository.save(reservation);
    }

    @Override
    public InventoryReservation updateReservation(Integer id, InventoryReservation reservationDetails) {
        InventoryReservation reservation = getReservationById(id);
        reservation.setReferenceType(reservationDetails.getReferenceType());
        reservation.setReferenceId(reservationDetails.getReferenceId());
        reservation.setStatus(reservationDetails.getStatus());
        reservation.setNotes(reservationDetails.getNotes());
        reservation.setUpdatedAt(LocalDateTime.now());
        reservation.setUpdatedBy(getCurrentUserId());
        return inventoryReservationRepository.save(reservation);
    }

    @Override
    public void deleteReservation(Integer id) {
        InventoryReservation reservation = getReservationById(id);
        reservation.setIsDeleted(true);
        reservation.setUpdatedAt(LocalDateTime.now());
        reservation.setUpdatedBy(getCurrentUserId());
        inventoryReservationRepository.save(reservation);
    }

    @Override
    public List<InventoryReservationDetail> getReservationDetailsByReservationId(Integer reservationId) {
        return inventoryReservationDetailRepository.findByReservationId(reservationId);
    }

    @Override
    public InventoryReservation reserveStock(Integer itemId, BigDecimal quantity, String referenceType, Integer referenceId, Integer userId) {
        // Get the inventory item
        InventoryItem item = inventoryItemService.getItemById(itemId);
        
        // Calculate available stock (on hand - allocated)
        BigDecimal availableStock = item.getQuantityOnHand().subtract(item.getQuantityAllocated());
        
        // Validate that we have sufficient stock
        if (availableStock.compareTo(quantity) < 0) {
            throw new InsufficientStockException(
                "InventoryItem", 
                itemId, 
                "reserve stock", 
                availableStock.toString(), 
                quantity.toString()
            );
        }
        
        // Create the reservation record
        InventoryReservation reservation = new InventoryReservation();
        reservation.setReferenceType(referenceType);
        reservation.setReferenceId(referenceId);
        reservation.setStatus("RESERVED");
        reservation.setCreatedBy(userId);
        reservation.setUpdatedBy(userId);
        
        // Save the reservation first to get its ID
        reservation = createReservation(reservation);
        
        // Create the reservation detail
        InventoryReservationDetail detail = new InventoryReservationDetail();
        detail.setReservationId(reservation.getId());
        detail.setItemId(itemId);
        detail.setReservedQuantity(quantity);
        detail.setLotNumber(item.getLotNumber());
        detail.setCreatedBy(userId);
        detail.setUpdatedBy(userId);
        
        inventoryReservationDetailRepository.save(detail);
        
        // Update the inventory item's allocated quantity
        item.setQuantityAllocated(item.getQuantityAllocated().add(quantity));
        item.setUpdatedAt(LocalDateTime.now());
        item.setUpdatedBy(userId);
        inventoryItemRepository.save(item);
        
        return reservation;
    }

    @Override
    public void releaseReservation(Integer reservationId, Integer userId) {
        // Get the reservation
        InventoryReservation reservation = getReservationById(reservationId);
        
        // Get all reservation details
        List<InventoryReservationDetail> details = getReservationDetailsByReservationId(reservationId);
        
        // Update each inventory item's allocated quantity
        for (InventoryReservationDetail detail : details) {
            InventoryItem item = inventoryItemService.getItemById(detail.getItemId());
            item.setQuantityAllocated(item.getQuantityAllocated().subtract(detail.getReservedQuantity()));
            item.setUpdatedAt(LocalDateTime.now());
            item.setUpdatedBy(userId);
            inventoryItemRepository.save(item);
        }
        
        // Update the reservation status
        reservation.setStatus("RELEASED");
        reservation.setUpdatedAt(LocalDateTime.now());
        reservation.setUpdatedBy(userId);
        inventoryReservationRepository.save(reservation);
    }

    @Override
    public void confirmReservation(Integer reservationId, Integer userId) {
        // Get the reservation
        InventoryReservation reservation = getReservationById(reservationId);
        
        // Get all reservation details
        List<InventoryReservationDetail> details = getReservationDetailsByReservationId(reservationId);
        
        // Update each inventory item's quantities
        for (InventoryReservationDetail detail : details) {
            InventoryItem item = inventoryItemService.getItemById(detail.getItemId());
            // Reduce both on-hand and allocated quantities
            item.setQuantityOnHand(item.getQuantityOnHand().subtract(detail.getReservedQuantity()));
            item.setQuantityAllocated(item.getQuantityAllocated().subtract(detail.getReservedQuantity()));
            item.setUpdatedAt(LocalDateTime.now());
            item.setUpdatedBy(userId);
            inventoryItemRepository.save(item);
        }
        
        // Update the reservation status
        reservation.setStatus("CONFIRMED");
        reservation.setUpdatedAt(LocalDateTime.now());
        reservation.setUpdatedBy(userId);
        inventoryReservationRepository.save(reservation);
    }

    private Integer getCurrentUserId() {
        // In a real implementation, this would come from the security context
        return 1;
    }
}