package com.superware.wms.inventory.controller;

import com.superware.wms.inventory.dto.InventoryCountDetailDto;
import com.superware.wms.inventory.dto.InventoryCountDto;
import com.superware.wms.inventory.entity.InventoryCount;
import com.superware.wms.inventory.entity.InventoryCountDetail;
import com.superware.wms.inventory.service.InventoryCountService;
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
 * REST controller for managing inventory counts.
 */
@RestController
@RequestMapping("/api/v1/inventory/counts")
@Tag(name = "Inventory Counts", description = "APIs for managing inventory counts")
public class InventoryCountController {

    private final InventoryCountService inventoryCountService;

    @Autowired
    public InventoryCountController(InventoryCountService inventoryCountService) {
        this.inventoryCountService = inventoryCountService;
    }

    /**
     * GET /api/v1/inventory/counts : Get all inventory counts with pagination
     *
     * @param pageable Pagination and sorting parameters
     * @return Paginated list of inventory counts
     */
    @GetMapping
    @Operation(
        summary = "Get all inventory counts with pagination",
        description = "Retrieves a paginated list of all inventory counts in the system."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved inventory counts",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<InventoryCountDto>> getAllCounts(
            @Parameter(description = "Pagination and sorting parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<InventoryCount> counts = inventoryCountService.getAllCounts(pageable);
        Page<InventoryCountDto> countDtos = counts.map(this::convertToDto);
        return ResponseEntity.ok(countDtos);
    }

    /**
     * GET /api/v1/inventory/counts/{id} : Get an inventory count by ID
     *
     * @param id The ID of the inventory count to retrieve
     * @return The requested inventory count
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get an inventory count by ID",
        description = "Retrieves a specific inventory count by its unique identifier."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved the inventory count",
            content = @Content(schema = @Schema(implementation = InventoryCountDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "Inventory count not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InventoryCountDto> getCountById(
            @Parameter(description = "ID of the inventory count to be retrieved", required = true)
            @PathVariable Integer id) {
        InventoryCount count = inventoryCountService.getCountById(id);
        return ResponseEntity.ok(convertToDto(count));
    }

    /**
     * POST /api/v1/inventory/counts : Create a new inventory count
     *
     * @param countDto The inventory count to create
     * @return The created inventory count
     */
    @PostMapping
    @Operation(
        summary = "Create a new inventory count",
        description = "Creates a new inventory count with the provided details."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Inventory count created successfully",
            content = @Content(schema = @Schema(implementation = InventoryCountDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InventoryCountDto> createCount(
            @Parameter(description = "Inventory count to be created", required = true)
            @Valid @RequestBody InventoryCountDto countDto) {
        InventoryCount count = convertToEntity(countDto);
        InventoryCount createdCount = inventoryCountService.createCount(count);
        
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(createdCount.getCountId())
            .toUri();
            
        return ResponseEntity.created(location).body(convertToDto(createdCount));
    }

    /**
     * PUT /api/v1/inventory/counts/{id} : Update an existing inventory count
     *
     * @param id The ID of the inventory count to update
     * @param countDto The updated inventory count data
     * @return The updated inventory count
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update an existing inventory count",
        description = "Updates an existing inventory count with the provided details."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Inventory count updated successfully",
            content = @Content(schema = @Schema(implementation = InventoryCountDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Inventory count not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InventoryCountDto> updateCount(
            @Parameter(description = "ID of the inventory count to be updated", required = true)
            @PathVariable Integer id,
            @Parameter(description = "Updated inventory count data", required = true)
            @Valid @RequestBody InventoryCountDto countDto) {
        InventoryCount count = convertToEntity(countDto);
        InventoryCount updatedCount = inventoryCountService.updateCount(id, count);
        return ResponseEntity.ok(convertToDto(updatedCount));
    }

    /**
     * DELETE /api/v1/inventory/counts/{id} : Delete an inventory count
     *
     * @param id The ID of the inventory count to delete
     * @return No content
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete an inventory count",
        description = "Deletes an inventory count by its unique identifier."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Inventory count deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Inventory count not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deleteCount(
            @Parameter(description = "ID of the inventory count to be deleted", required = true)
            @PathVariable Integer id) {
        inventoryCountService.deleteCount(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/inventory/counts/{countId}/details : Get count details by count ID
     *
     * @param countId The ID of the count
     * @return List of inventory count details
     */
    @GetMapping("/{countId}/details")
    @Operation(
        summary = "Get count details by count ID",
        description = "Retrieves all inventory count details for a specific count."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved inventory count details",
            content = @Content(schema = @Schema(implementation = InventoryCountDetailDto.class, type = "array"))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<InventoryCountDetailDto>> getCountDetailsByCountId(
            @Parameter(description = "ID of the count", required = true)
            @PathVariable Integer countId) {
        List<InventoryCountDetail> details = inventoryCountService.getCountDetailsByCountId(countId);
        List<InventoryCountDetailDto> detailDtos = details.stream()
                .map(this::convertDetailToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(detailDtos);
    }

    /**
     * POST /api/v1/inventory/counts/start : Start a new inventory count
     *
     * @param locationId The ID of the location to count
     * @param userId The ID of the user starting the count
     * @return The created inventory count
     */
    @PostMapping("/start")
    @Operation(
        summary = "Start a new inventory count",
        description = "Starts a new inventory count for a specific location."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Inventory count started successfully",
            content = @Content(schema = @Schema(implementation = InventoryCountDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InventoryCountDto> startCount(
            @Parameter(description = "ID of the location to count", required = true)
            @RequestParam Integer locationId,
            @Parameter(description = "ID of the user starting the count", required = true)
            @RequestParam Integer userId) {
        InventoryCount count = inventoryCountService.startCount(locationId, userId);
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(count.getCountId())
            .toUri();
        return ResponseEntity.created(location).body(convertToDto(count));
    }

    /**
     * POST /api/v1/inventory/counts/{countId}/detail : Add a count detail to an inventory count
     *
     * @param countId The ID of the inventory count
     * @param itemId The ID of the inventory item
     * @param countedQuantity The quantity counted
     * @param userId The ID of the user performing the count
     * @return The created inventory count detail
     */
    @PostMapping("/{countId}/detail")
    @Operation(
        summary = "Add a count detail to an inventory count",
        description = "Adds a count detail to an inventory count."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Count detail added successfully",
            content = @Content(schema = @Schema(implementation = InventoryCountDetailDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Inventory count or item not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InventoryCountDetailDto> addCountDetail(
            @Parameter(description = "ID of the inventory count", required = true)
            @PathVariable Integer countId,
            @Parameter(description = "ID of the inventory item", required = true)
            @RequestParam Integer itemId,
            @Parameter(description = "Quantity counted", required = true)
            @RequestParam BigDecimal countedQuantity,
            @Parameter(description = "ID of the user performing the count", required = true)
            @RequestParam Integer userId) {
        InventoryCountDetail detail = inventoryCountService.addCountDetail(countId, itemId, countedQuantity, userId);
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(detail.getCountDetailId())
            .toUri();
        return ResponseEntity.created(location).body(convertDetailToDto(detail));
    }

    /**
     * POST /api/v1/inventory/counts/{id}/complete : Complete an inventory count
     *
     * @param id The ID of the inventory count to complete
     * @param userId The ID of the user completing the count
     * @return No content
     */
    @PostMapping("/{id}/complete")
    @Operation(
        summary = "Complete an inventory count",
        description = "Completes an inventory count and processes any variances."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Inventory count completed successfully"),
        @ApiResponse(responseCode = "404", description = "Inventory count not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> completeCount(
            @Parameter(description = "ID of the inventory count to complete", required = true)
            @PathVariable Integer id,
            @Parameter(description = "ID of the user completing the count", required = true)
            @RequestParam Integer userId) {
        inventoryCountService.completeCount(id, userId);
        return ResponseEntity.noContent().build();
    }

    // Helper methods for conversion between Entity and DTO
    private InventoryCountDto convertToDto(InventoryCount count) {
        InventoryCountDto dto = new InventoryCountDto();
        dto.setCountId(count.getCountId());
        dto.setTenantId(count.getTenantId());
        dto.setCountNumber(count.getCountNumber());
        dto.setCountType(count.getCountType());
        dto.setStatus(count.getStatus());
        dto.setStartDate(count.getStartDate());
        dto.setEndDate(count.getEndDate());
        dto.setFacilityId(count.getFacilityId());
        dto.setZoneId(count.getZoneId());
        dto.setLocationId(count.getLocationId());
        dto.setProductId(count.getProductId());
        dto.setCategoryId(count.getCategoryId());
        dto.setNotes(count.getNotes());
        dto.setIsApproved(count.getIsApproved());
        dto.setApprovedBy(count.getApprovedBy());
        dto.setApprovedAt(count.getApprovedAt());
        dto.setCreatedAt(count.getCreatedAt());
        dto.setUpdatedAt(count.getUpdatedAt());
        dto.setCreatedBy(count.getCreatedBy());
        dto.setUpdatedBy(count.getUpdatedBy());
        dto.setIsDeleted(count.getIsDeleted());
        return dto;
    }

    private InventoryCount convertToEntity(InventoryCountDto dto) {
        InventoryCount count = new InventoryCount();
        count.setCountId(dto.getCountId());
        count.setTenantId(dto.getTenantId());
        count.setCountNumber(dto.getCountNumber());
        count.setCountType(dto.getCountType());
        count.setStatus(dto.getStatus());
        count.setStartDate(dto.getStartDate());
        count.setEndDate(dto.getEndDate());
        count.setFacilityId(dto.getFacilityId());
        count.setZoneId(dto.getZoneId());
        count.setLocationId(dto.getLocationId());
        count.setProductId(dto.getProductId());
        count.setCategoryId(dto.getCategoryId());
        count.setNotes(dto.getNotes());
        count.setIsApproved(dto.getIsApproved());
        count.setApprovedBy(dto.getApprovedBy());
        count.setApprovedAt(dto.getApprovedAt());
        count.setCreatedAt(dto.getCreatedAt());
        count.setUpdatedAt(dto.getUpdatedAt());
        count.setCreatedBy(dto.getCreatedBy());
        count.setUpdatedBy(dto.getUpdatedBy());
        count.setIsDeleted(dto.getIsDeleted());
        return count;
    }

    private InventoryCountDetailDto convertDetailToDto(InventoryCountDetail detail) {
        InventoryCountDetailDto dto = new InventoryCountDetailDto();
        dto.setCountDetailId(detail.getCountDetailId());
        dto.setCountId(detail.getCountId());
        dto.setItemId(detail.getItemId());
        dto.setExpectedQuantity(detail.getExpectedQuantity());
        dto.setCountedQuantity(detail.getCountedQuantity());
        dto.setVariance(detail.getVariance());
        dto.setUnitOfMeasure(detail.getUnitOfMeasure());
        dto.setLotNumber(detail.getLotNumber());
        dto.setNotes(detail.getNotes());
        dto.setIsRecounted(detail.getIsRecounted());
        dto.setCreatedAt(detail.getCreatedAt());
        dto.setUpdatedAt(detail.getUpdatedAt());
        dto.setCreatedBy(detail.getCreatedBy());
        dto.setUpdatedBy(detail.getUpdatedBy());
        dto.setIsDeleted(detail.getIsDeleted());
        return dto;
    }
}