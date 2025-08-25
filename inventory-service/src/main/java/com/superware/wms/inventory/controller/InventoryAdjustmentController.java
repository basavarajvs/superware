package com.superware.wms.inventory.controller;

import com.superware.wms.inventory.dto.InventoryAdjustmentDto;
import com.superware.wms.inventory.dto.InventoryAdjustmentDetailDto;
import com.superware.wms.inventory.entity.InventoryAdjustment;
import com.superware.wms.inventory.entity.InventoryAdjustmentDetail;
import com.superware.wms.inventory.service.InventoryAdjustmentService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing inventory adjustments.
 */
@RestController
@RequestMapping("/api/v1/inventory/adjustments")
@Tag(name = "Inventory Adjustments", description = "APIs for managing inventory adjustments")
public class InventoryAdjustmentController {

    private final InventoryAdjustmentService inventoryAdjustmentService;

    @Autowired
    public InventoryAdjustmentController(InventoryAdjustmentService inventoryAdjustmentService) {
        this.inventoryAdjustmentService = inventoryAdjustmentService;
    }

    /**
     * GET /api/v1/inventory/adjustments : Get all inventory adjustments with pagination
     *
     * @param pageable Pagination and sorting parameters
     * @return Paginated list of inventory adjustments
     */
    @GetMapping
    @Operation(
        summary = "Get all inventory adjustments with pagination",
        description = "Retrieves a paginated list of all inventory adjustments in the system."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved inventory adjustments",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<InventoryAdjustmentDto>> getAllAdjustments(
            @Parameter(description = "Pagination and sorting parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<InventoryAdjustment> adjustments = inventoryAdjustmentService.getAllAdjustments(pageable);
        Page<InventoryAdjustmentDto> adjustmentDtos = adjustments.map(this::convertToDto);
        return ResponseEntity.ok(adjustmentDtos);
    }

    /**
     * GET /api/v1/inventory/adjustments/{id} : Get an inventory adjustment by ID
     *
     * @param id The ID of the inventory adjustment to retrieve
     * @return The requested inventory adjustment
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get an inventory adjustment by ID",
        description = "Retrieves a specific inventory adjustment by its unique identifier."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved the inventory adjustment",
            content = @Content(schema = @Schema(implementation = InventoryAdjustmentDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "Inventory adjustment not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InventoryAdjustmentDto> getAdjustmentById(
            @Parameter(description = "ID of the inventory adjustment to be retrieved", required = true)
            @PathVariable Integer id) {
        InventoryAdjustment adjustment = inventoryAdjustmentService.getAdjustmentById(id);
        return ResponseEntity.ok(convertToDto(adjustment));
    }

    /**
     * POST /api/v1/inventory/adjustments : Create a new inventory adjustment
     *
     * @param adjustmentDto The inventory adjustment to create
     * @return The created inventory adjustment
     */
    @PostMapping
    @Operation(
        summary = "Create a new inventory adjustment",
        description = "Creates a new inventory adjustment with the provided details."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Inventory adjustment created successfully",
            content = @Content(schema = @Schema(implementation = InventoryAdjustmentDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InventoryAdjustmentDto> createAdjustment(
            @Parameter(description = "Inventory adjustment to be created", required = true)
            @Valid @RequestBody InventoryAdjustmentDto adjustmentDto) {
        InventoryAdjustment adjustment = convertToEntity(adjustmentDto);
        InventoryAdjustment createdAdjustment = inventoryAdjustmentService.createAdjustment(adjustment);
        
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(createdAdjustment.getAdjustmentId())
            .toUri();
            
        return ResponseEntity.created(location).body(convertToDto(createdAdjustment));
    }

    /**
     * PUT /api/v1/inventory/adjustments/{id} : Update an existing inventory adjustment
     *
     * @param id The ID of the inventory adjustment to update
     * @param adjustmentDto The updated inventory adjustment data
     * @return The updated inventory adjustment
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update an existing inventory adjustment",
        description = "Updates an existing inventory adjustment with the provided details."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Inventory adjustment updated successfully",
            content = @Content(schema = @Schema(implementation = InventoryAdjustmentDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Inventory adjustment not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InventoryAdjustmentDto> updateAdjustment(
            @Parameter(description = "ID of the inventory adjustment to be updated", required = true)
            @PathVariable Integer id,
            @Parameter(description = "Updated inventory adjustment data", required = true)
            @Valid @RequestBody InventoryAdjustmentDto adjustmentDto) {
        InventoryAdjustment adjustment = convertToEntity(adjustmentDto);
        InventoryAdjustment updatedAdjustment = inventoryAdjustmentService.updateAdjustment(id, adjustment);
        return ResponseEntity.ok(convertToDto(updatedAdjustment));
    }

    /**
     * DELETE /api/v1/inventory/adjustments/{id} : Delete an inventory adjustment
     *
     * @param id The ID of the inventory adjustment to delete
     * @return No content
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete an inventory adjustment",
        description = "Deletes an inventory adjustment by its unique identifier."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Inventory adjustment deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Inventory adjustment not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deleteAdjustment(
            @Parameter(description = "ID of the inventory adjustment to be deleted", required = true)
            @PathVariable Integer id) {
        inventoryAdjustmentService.deleteAdjustment(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/inventory/adjustments/{adjustmentId}/details : Get adjustment details by adjustment ID
     *
     * @param adjustmentId The ID of the adjustment
     * @return List of inventory adjustment details
     */
    @GetMapping("/{adjustmentId}/details")
    @Operation(
        summary = "Get adjustment details by adjustment ID",
        description = "Retrieves all inventory adjustment details for a specific adjustment."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved inventory adjustment details",
            content = @Content(schema = @Schema(implementation = InventoryAdjustmentDetailDto.class, type = "array"))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<InventoryAdjustmentDetailDto>> getAdjustmentDetailsByAdjustmentId(
            @Parameter(description = "ID of the adjustment", required = true)
            @PathVariable Integer adjustmentId) {
        List<InventoryAdjustmentDetail> details = inventoryAdjustmentService.getAdjustmentDetailsByAdjustmentId(adjustmentId);
        List<InventoryAdjustmentDetailDto> detailDtos = details.stream()
                .map(this::convertDetailToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(detailDtos);
    }

    /**
     * POST /api/v1/inventory/adjustments/stock : Adjust stock for an inventory item
     *
     * @param itemId The ID of the inventory item
     * @param quantity The quantity to adjust (positive for increase, negative for decrease)
     * @param reason The reason for the adjustment
     * @param userId The ID of the user performing the adjustment
     * @return The created inventory adjustment
     */
    @PostMapping("/stock")
    @Operation(
        summary = "Adjust stock for an inventory item",
        description = "Adjusts stock for an inventory item by creating an adjustment record."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Stock adjusted successfully",
            content = @Content(schema = @Schema(implementation = InventoryAdjustmentDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Inventory item not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InventoryAdjustmentDto> adjustStock(
            @Parameter(description = "ID of the inventory item", required = true)
            @RequestParam Integer itemId,
            @Parameter(description = "Quantity to adjust (positive for increase, negative for decrease)", required = true)
            @RequestParam BigDecimal quantity,
            @Parameter(description = "Reason for the adjustment", required = true)
            @RequestParam String reason,
            @Parameter(description = "ID of the user performing the adjustment", required = true)
            @RequestParam Integer userId) {
        InventoryAdjustment adjustment = inventoryAdjustmentService.adjustStock(itemId, quantity, reason, userId);
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(adjustment.getAdjustmentId())
            .toUri();
        return ResponseEntity.created(location).body(convertToDto(adjustment));
    }

    // Helper methods for conversion between Entity and DTO
    private InventoryAdjustmentDto convertToDto(InventoryAdjustment adjustment) {
        InventoryAdjustmentDto dto = new InventoryAdjustmentDto();
        dto.setAdjustmentId(adjustment.getAdjustmentId());
        dto.setTenantId(adjustment.getTenantId());
        dto.setAdjustmentNumber(adjustment.getAdjustmentNumber());
        dto.setAdjustmentDate(adjustment.getAdjustmentDate());
        dto.setStatus(adjustment.getStatus());
        dto.setAdjustmentType(adjustment.getAdjustmentType());
        dto.setReasonCode(adjustment.getReasonCode());
        dto.setReferenceNumber(adjustment.getReferenceNumber());
        dto.setReferenceType(adjustment.getReferenceType());
        dto.setReferenceId(adjustment.getReferenceId());
        dto.setNotes(adjustment.getNotes());
        dto.setIsApproved(adjustment.getIsApproved());
        dto.setApprovedBy(adjustment.getApprovedBy());
        dto.setApprovedAt(adjustment.getApprovedAt());
        dto.setCreatedAt(adjustment.getCreatedAt());
        dto.setUpdatedAt(adjustment.getUpdatedAt());
        dto.setCreatedBy(adjustment.getCreatedBy());
        dto.setUpdatedBy(adjustment.getUpdatedBy());
        dto.setIsDeleted(adjustment.getIsDeleted());
        return dto;
    }

    private InventoryAdjustment convertToEntity(InventoryAdjustmentDto dto) {
        InventoryAdjustment adjustment = new InventoryAdjustment();
        adjustment.setAdjustmentId(dto.getAdjustmentId());
        adjustment.setTenantId(dto.getTenantId());
        adjustment.setAdjustmentNumber(dto.getAdjustmentNumber());
        adjustment.setAdjustmentDate(dto.getAdjustmentDate());
        adjustment.setStatus(dto.getStatus());
        adjustment.setAdjustmentType(dto.getAdjustmentType());
        adjustment.setReasonCode(dto.getReasonCode());
        adjustment.setReferenceNumber(dto.getReferenceNumber());
        adjustment.setReferenceType(dto.getReferenceType());
        adjustment.setReferenceId(dto.getReferenceId());
        adjustment.setNotes(dto.getNotes());
        adjustment.setIsApproved(dto.getIsApproved());
        adjustment.setApprovedBy(dto.getApprovedBy());
        adjustment.setApprovedAt(dto.getApprovedAt());
        adjustment.setCreatedAt(dto.getCreatedAt());
        adjustment.setUpdatedAt(dto.getUpdatedAt());
        adjustment.setCreatedBy(dto.getCreatedBy());
        adjustment.setUpdatedBy(dto.getUpdatedBy());
        adjustment.setIsDeleted(dto.getIsDeleted());
        return adjustment;
    }

    private InventoryAdjustmentDetailDto convertDetailToDto(InventoryAdjustmentDetail detail) {
        InventoryAdjustmentDetailDto dto = new InventoryAdjustmentDetailDto();
        dto.setAdjustmentDetailId(detail.getAdjustmentDetailId());
        dto.setAdjustmentId(detail.getAdjustmentId());
        dto.setItemId(detail.getItemId());
        dto.setLocationId(detail.getLocationId());
        dto.setLotNumber(detail.getLotNumber());
        dto.setSerialNumber(detail.getSerialNumber());
        dto.setQuantityBefore(detail.getQuantityBefore());
        dto.setQuantityAfter(detail.getQuantityAfter());
        dto.setQuantityAdjusted(detail.getQuantityAdjusted());
        dto.setUnitOfMeasure(detail.getUnitOfMeasure());
        dto.setUnitCost(detail.getUnitCost());
        dto.setTotalCost(detail.getTotalCost());
        dto.setNotes(detail.getNotes());
        dto.setCreatedAt(detail.getCreatedAt());
        dto.setUpdatedAt(detail.getUpdatedAt());
        dto.setCreatedBy(detail.getCreatedBy());
        dto.setUpdatedBy(detail.getUpdatedBy());
        dto.setIsDeleted(detail.getIsDeleted());
        return dto;
    }
}