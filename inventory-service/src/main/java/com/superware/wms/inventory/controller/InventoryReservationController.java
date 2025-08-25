package com.superware.wms.inventory.controller;

import com.superware.wms.inventory.dto.InventoryReservationDto;
import com.superware.wms.inventory.dto.InventoryReservationDetailDto;
import com.superware.wms.inventory.entity.InventoryReservation;
import com.superware.wms.inventory.entity.InventoryReservationDetail;
import com.superware.wms.inventory.service.InventoryReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing inventory reservations.
 */
@RestController
@RequestMapping("/api/v1/inventory/reservations")
@Tag(name = "Inventory Reservations", description = "APIs for managing inventory reservations")
public class InventoryReservationController {

    private final InventoryReservationService inventoryReservationService;

    @Autowired
    public InventoryReservationController(InventoryReservationService inventoryReservationService) {
        this.inventoryReservationService = inventoryReservationService;
    }

    /**
     * GET /api/v1/inventory/reservations : Get all inventory reservations with pagination
     *
     * @param pageable Pagination and sorting parameters
     * @return Paginated list of inventory reservations
     */
    @GetMapping
    @Operation(
        summary = "Get all inventory reservations with pagination",
        description = "Retrieves a paginated list of all inventory reservations in the system."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved inventory reservations",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<InventoryReservationDto>> getAllReservations(
            @Parameter(description = "Pagination and sorting parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<InventoryReservation> reservations = inventoryReservationService.getAllReservations(pageable);
        Page<InventoryReservationDto> reservationDtos = reservations.map(this::convertToDto);
        return ResponseEntity.ok(reservationDtos);
    }

    /**
     * GET /api/v1/inventory/reservations/{id} : Get an inventory reservation by ID
     *
     * @param id The ID of the inventory reservation to retrieve
     * @return The requested inventory reservation
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get an inventory reservation by ID",
        description = "Retrieves a specific inventory reservation by its unique identifier."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved the inventory reservation",
            content = @Content(schema = @Schema(implementation = InventoryReservationDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "Inventory reservation not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InventoryReservationDto> getReservationById(
            @Parameter(description = "ID of the inventory reservation to be retrieved", required = true)
            @PathVariable Integer id) {
        InventoryReservation reservation = inventoryReservationService.getReservationById(id);
        return ResponseEntity.ok(convertToDto(reservation));
    }

    /**
     * POST /api/v1/inventory/reservations : Create a new inventory reservation
     *
     * @param reservationDto The inventory reservation to create
     * @return The created inventory reservation
     */
    @PostMapping
    @Operation(
        summary = "Create a new inventory reservation",
        description = "Creates a new inventory reservation with the provided details."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Inventory reservation created successfully",
            content = @Content(schema = @Schema(implementation = InventoryReservationDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InventoryReservationDto> createReservation(
            @Parameter(description = "Inventory reservation to be created", required = true)
            @Valid @RequestBody InventoryReservationDto reservationDto) {
        InventoryReservation reservation = convertToEntity(reservationDto);
        InventoryReservation createdReservation = inventoryReservationService.createReservation(reservation);
        
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(createdReservation.getReservationId())
            .toUri();
            
        return ResponseEntity.created(location).body(convertToDto(createdReservation));
    }

    /**
     * PUT /api/v1/inventory/reservations/{id} : Update an existing inventory reservation
     *
     * @param id The ID of the inventory reservation to update
     * @param reservationDto The updated inventory reservation data
     * @return The updated inventory reservation
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update an existing inventory reservation",
        description = "Updates an existing inventory reservation with the provided details."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Inventory reservation updated successfully",
            content = @Content(schema = @Schema(implementation = InventoryReservationDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Inventory reservation not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InventoryReservationDto> updateReservation(
            @Parameter(description = "ID of the inventory reservation to be updated", required = true)
            @PathVariable Integer id,
            @Parameter(description = "Updated inventory reservation data", required = true)
            @Valid @RequestBody InventoryReservationDto reservationDto) {
        InventoryReservation reservation = convertToEntity(reservationDto);
        InventoryReservation updatedReservation = inventoryReservationService.updateReservation(id, reservation);
        return ResponseEntity.ok(convertToDto(updatedReservation));
    }

    /**
     * DELETE /api/v1/inventory/reservations/{id} : Delete an inventory reservation
     *
     * @param id The ID of the inventory reservation to delete
     * @return No content
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete an inventory reservation",
        description = "Deletes an inventory reservation by its unique identifier."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Inventory reservation deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Inventory reservation not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deleteReservation(
            @Parameter(description = "ID of the inventory reservation to be deleted", required = true)
            @PathVariable Integer id) {
        inventoryReservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/inventory/reservations/{reservationId}/details : Get reservation details by reservation ID
     *
     * @param reservationId The ID of the reservation
     * @return List of inventory reservation details
     */
    @GetMapping("/{reservationId}/details")
    @Operation(
        summary = "Get reservation details by reservation ID",
        description = "Retrieves all inventory reservation details for a specific reservation."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved inventory reservation details",
            content = @Content(schema = @Schema(implementation = InventoryReservationDetailDto.class, type = "array"))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<InventoryReservationDetailDto>> getReservationDetailsByReservationId(
            @Parameter(description = "ID of the reservation", required = true)
            @PathVariable Integer reservationId) {
        List<InventoryReservationDetail> details = inventoryReservationService.getReservationDetailsByReservationId(reservationId);
        List<InventoryReservationDetailDto> detailDtos = details.stream()
                .map(this::convertDetailToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(detailDtos);
    }

    /**
     * POST /api/v1/inventory/reservations/stock : Reserve stock for an inventory item
     *
     * @param itemId The ID of the inventory item
     * @param quantity The quantity to reserve
     * @param referenceType The type of reference (e.g., ORDER, WORK_ORDER)
     * @param referenceId The ID of the reference
     * @param userId The ID of the user performing the reservation
     * @return The created inventory reservation
     */
    @PostMapping("/stock")
    @Operation(
        summary = "Reserve stock for an inventory item",
        description = "Reserves stock for an inventory item by creating a reservation record."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Stock reserved successfully",
            content = @Content(schema = @Schema(implementation = InventoryReservationDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Inventory item not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InventoryReservationDto> reserveStock(
            @Parameter(description = "ID of the inventory item", required = true)
            @RequestParam Integer itemId,
            @Parameter(description = "Quantity to reserve", required = true)
            @RequestParam BigDecimal quantity,
            @Parameter(description = "Type of reference (e.g., ORDER, WORK_ORDER)", required = true)
            @RequestParam String referenceType,
            @Parameter(description = "ID of the reference", required = true)
            @RequestParam Integer referenceId,
            @Parameter(description = "ID of the user performing the reservation", required = true)
            @RequestParam Integer userId) {
        InventoryReservation reservation = inventoryReservationService.reserveStock(itemId, quantity, referenceType, referenceId, userId);
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(reservation.getReservationId())
            .toUri();
        return ResponseEntity.created(location).body(convertToDto(reservation));
    }

    /**
     * POST /api/v1/inventory/reservations/{id}/release : Release a reserved stock
     *
     * @param id The ID of the reservation to release
     * @param userId The ID of the user performing the release
     * @return No content
     */
    @PostMapping("/{id}/release")
    @Operation(
        summary = "Release a reserved stock",
        description = "Releases a reserved stock."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Stock released successfully"),
        @ApiResponse(responseCode = "404", description = "Inventory reservation not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> releaseReservation(
            @Parameter(description = "ID of the reservation to release", required = true)
            @PathVariable Integer id,
            @Parameter(description = "ID of the user performing the release", required = true)
            @RequestParam Integer userId) {
        inventoryReservationService.releaseReservation(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/v1/inventory/reservations/{id}/confirm : Confirm a reservation
     *
     * @param id The ID of the reservation to confirm
     * @param userId The ID of the user confirming the reservation
     * @return No content
     */
    @PostMapping("/{id}/confirm")
    @Operation(
        summary = "Confirm a reservation",
        description = "Confirms a reservation (converts it to an actual allocation)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Reservation confirmed successfully"),
        @ApiResponse(responseCode = "404", description = "Inventory reservation not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> confirmReservation(
            @Parameter(description = "ID of the reservation to confirm", required = true)
            @PathVariable Integer id,
            @Parameter(description = "ID of the user confirming the reservation", required = true)
            @RequestParam Integer userId) {
        inventoryReservationService.confirmReservation(id, userId);
        return ResponseEntity.noContent().build();
    }

    // Helper methods for conversion between Entity and DTO
    private InventoryReservationDto convertToDto(InventoryReservation reservation) {
        InventoryReservationDto dto = new InventoryReservationDto();
        dto.setReservationId(reservation.getReservationId());
        dto.setTenantId(reservation.getTenantId());
        dto.setReservationType(reservation.getReservationType());
        dto.setStatus(reservation.getStatus());
        dto.setReferenceNumber(reservation.getReferenceNumber());
        dto.setReferenceType(reservation.getReferenceType());
        dto.setReferenceId(reservation.getReferenceId());
        dto.setRequestedDate(reservation.getRequestedDate());
        dto.setExpiryDate(reservation.getExpiryDate());
        dto.setPriority(reservation.getPriority());
        dto.setNotes(reservation.getNotes());
        dto.setCreatedAt(reservation.getCreatedAt());
        dto.setUpdatedAt(reservation.getUpdatedAt());
        dto.setCreatedBy(reservation.getCreatedBy());
        dto.setUpdatedBy(reservation.getUpdatedBy());
        dto.setIsDeleted(reservation.getIsDeleted());
        return dto;
    }

    private InventoryReservation convertToEntity(InventoryReservationDto dto) {
        InventoryReservation reservation = new InventoryReservation();
        reservation.setReservationId(dto.getReservationId());
        reservation.setTenantId(dto.getTenantId());
        reservation.setReservationType(dto.getReservationType());
        reservation.setStatus(dto.getStatus());
        reservation.setReferenceNumber(dto.getReferenceNumber());
        reservation.setReferenceType(dto.getReferenceType());
        reservation.setReferenceId(dto.getReferenceId());
        reservation.setRequestedDate(dto.getRequestedDate());
        reservation.setExpiryDate(dto.getExpiryDate());
        reservation.setPriority(dto.getPriority());
        reservation.setNotes(dto.getNotes());
        reservation.setCreatedAt(dto.getCreatedAt());
        reservation.setUpdatedAt(dto.getUpdatedAt());
        reservation.setCreatedBy(dto.getCreatedBy());
        reservation.setUpdatedBy(dto.getUpdatedBy());
        reservation.setIsDeleted(dto.getIsDeleted());
        return reservation;
    }

    private InventoryReservationDetailDto convertDetailToDto(InventoryReservationDetail detail) {
        InventoryReservationDetailDto dto = new InventoryReservationDetailDto();
        dto.setReservationDetailId(detail.getReservationDetailId());
        dto.setReservationId(detail.getReservationId());
        dto.setItemId(detail.getItemId());
        dto.setQuantityRequested(detail.getQuantityRequested());
        dto.setQuantityAllocated(detail.getQuantityAllocated());
        dto.setQuantityFulfilled(detail.getQuantityFulfilled());
        dto.setUnitOfMeasure(detail.getUnitOfMeasure());
        dto.setLotNumber(detail.getLotNumber());
        dto.setNotes(detail.getNotes());
        dto.setCreatedAt(detail.getCreatedAt());
        dto.setUpdatedAt(detail.getUpdatedAt());
        dto.setCreatedBy(detail.getCreatedBy());
        dto.setUpdatedBy(detail.getUpdatedBy());
        dto.setIsDeleted(detail.getIsDeleted());
        return dto;
    }
}