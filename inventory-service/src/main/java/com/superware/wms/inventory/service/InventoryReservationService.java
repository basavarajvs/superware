package com.superware.wms.inventory.service;

import com.superware.wms.inventory.entity.InventoryReservation;
import com.superware.wms.inventory.entity.InventoryReservationDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for managing inventory reservations.
 */
public interface InventoryReservationService {
    
    /**
     * Get all inventory reservations with pagination.
     *
     * @param pageable pagination information
     * @return page of inventory reservations
     */
    Page<InventoryReservation> getAllReservations(Pageable pageable);
    
    /**
     * Get an inventory reservation by ID.
     *
     * @param id the ID of the inventory reservation
     * @return the inventory reservation
     * @throws com.superware.wms.inventory.exception.ResourceNotFoundException if the inventory reservation is not found
     */
    InventoryReservation getReservationById(Integer id);
    
    /**
     * Create a new inventory reservation.
     *
     * @param reservation the inventory reservation to create
     * @return the created inventory reservation
     */
    InventoryReservation createReservation(InventoryReservation reservation);
    
    /**
     * Update an existing inventory reservation.
     *
     * @param id the ID of the inventory reservation to update
     * @param reservationDetails the updated inventory reservation data
     * @return the updated inventory reservation
     * @throws com.superware.wms.inventory.exception.ResourceNotFoundException if the inventory reservation is not found
     */
    InventoryReservation updateReservation(Integer id, InventoryReservation reservationDetails);
    
    /**
     * Delete an inventory reservation.
     *
     * @param id the ID of the inventory reservation to delete
     * @throws com.superware.wms.inventory.exception.ResourceNotFoundException if the inventory reservation is not found
     */
    void deleteReservation(Integer id);
    
    /**
     * Get all inventory reservation details for a specific reservation.
     *
     * @param reservationId the ID of the reservation
     * @return list of inventory reservation details
     */
    List<InventoryReservationDetail> getReservationDetailsByReservationId(Integer reservationId);
    
    /**
     * Reserve stock for an inventory item.
     *
     * @param itemId the ID of the inventory item
     * @param quantity the quantity to reserve
     * @param referenceType the type of reference (e.g., ORDER, WORK_ORDER)
     * @param referenceId the ID of the reference
     * @param userId the ID of the user performing the reservation
     * @return the created inventory reservation
     * @throws com.superware.wms.inventory.exception.ResourceNotFoundException if the inventory item is not found
     * @throws com.superware.wms.inventory.exception.InsufficientStockException if there is insufficient stock
     */
    InventoryReservation reserveStock(Integer itemId, java.math.BigDecimal quantity, String referenceType, Integer referenceId, Integer userId);
    
    /**
     * Release a reserved stock.
     *
     * @param reservationId the ID of the reservation to release
     * @param userId the ID of the user performing the release
     * @throws com.superware.wms.inventory.exception.ResourceNotFoundException if the reservation is not found
     */
    void releaseReservation(Integer reservationId, Integer userId);
    
    /**
     * Confirm a reservation (convert it to an actual allocation).
     *
     * @param reservationId the ID of the reservation to confirm
     * @param userId the ID of the user confirming the reservation
     * @throws com.superware.wms.inventory.exception.ResourceNotFoundException if the reservation is not found
     */
    void confirmReservation(Integer reservationId, Integer userId);
}