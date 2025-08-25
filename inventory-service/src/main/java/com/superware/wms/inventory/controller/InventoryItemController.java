package com.superware.wms.inventory.controller;

import com.superware.wms.inventory.dto.InventoryItemDto;
import com.superware.wms.inventory.entity.InventoryItem;
import com.superware.wms.inventory.service.InventoryItemService;
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
 * REST controller for managing inventory items.
 */
@RestController
@RequestMapping("/api/v1/inventory/items")
@Tag(name = "Inventory Items", description = "APIs for managing inventory items")
public class InventoryItemController {

    private final InventoryItemService inventoryItemService;

    @Autowired
    public InventoryItemController(InventoryItemService inventoryItemService) {
        this.inventoryItemService = inventoryItemService;
    }

    /**
     * GET /api/v1/inventory/items : Get all inventory items with pagination
     *
     * @param pageable Pagination and sorting parameters
     * @return Paginated list of inventory items
     */
    @GetMapping
    @Operation(
        summary = "Get all inventory items with pagination",
        description = "Retrieves a paginated list of all inventory items in the system."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved inventory items",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<InventoryItemDto>> getAllItems(
            @Parameter(description = "Pagination and sorting parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<InventoryItem> items = inventoryItemService.getAllItems(pageable);
        Page<InventoryItemDto> itemDtos = items.map(this::convertToDto);
        return ResponseEntity.ok(itemDtos);
    }

    /**
     * GET /api/v1/inventory/items/{id} : Get an inventory item by ID
     *
     * @param id The ID of the inventory item to retrieve
     * @return The requested inventory item
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get an inventory item by ID",
        description = "Retrieves a specific inventory item by its unique identifier."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved the inventory item",
            content = @Content(schema = @Schema(implementation = InventoryItemDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "Inventory item not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InventoryItemDto> getItemById(
            @Parameter(description = "ID of the inventory item to be retrieved", required = true)
            @PathVariable Integer id) {
        InventoryItem item = inventoryItemService.getItemById(id);
        return ResponseEntity.ok(convertToDto(item));
    }

    /**
     * POST /api/v1/inventory/items : Create a new inventory item
     *
     * @param itemDto The inventory item to create
     * @return The created inventory item
     */
    @PostMapping
    @Operation(
        summary = "Create a new inventory item",
        description = "Creates a new inventory item with the provided details."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Inventory item created successfully",
            content = @Content(schema = @Schema(implementation = InventoryItemDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InventoryItemDto> createItem(
            @Parameter(description = "Inventory item to be created", required = true)
            @Valid @RequestBody InventoryItemDto itemDto) {
        InventoryItem item = convertToEntity(itemDto);
        InventoryItem createdItem = inventoryItemService.createItem(item);
        
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(createdItem.getItemId())
            .toUri();
            
        return ResponseEntity.created(location).body(convertToDto(createdItem));
    }

    /**
     * PUT /api/v1/inventory/items/{id} : Update an existing inventory item
     *
     * @param id The ID of the inventory item to update
     * @param itemDto The updated inventory item data
     * @return The updated inventory item
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update an existing inventory item",
        description = "Updates an existing inventory item with the provided details."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Inventory item updated successfully",
            content = @Content(schema = @Schema(implementation = InventoryItemDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Inventory item not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InventoryItemDto> updateItem(
            @Parameter(description = "ID of the inventory item to be updated", required = true)
            @PathVariable Integer id,
            @Parameter(description = "Updated inventory item data", required = true)
            @Valid @RequestBody InventoryItemDto itemDto) {
        InventoryItem item = convertToEntity(itemDto);
        InventoryItem updatedItem = inventoryItemService.updateItem(id, item);
        return ResponseEntity.ok(convertToDto(updatedItem));
    }

    /**
     * DELETE /api/v1/inventory/items/{id} : Delete an inventory item
     *
     * @param id The ID of the inventory item to delete
     * @return No content
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete an inventory item",
        description = "Deletes an inventory item by its unique identifier."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Inventory item deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Inventory item not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deleteItem(
            @Parameter(description = "ID of the inventory item to be deleted", required = true)
            @PathVariable Integer id) {
        inventoryItemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/inventory/items/product/{productId} : Get inventory items by product ID
     *
     * @param productId The ID of the product to filter by
     * @return List of inventory items for the specified product
     */
    @GetMapping("/product/{productId}")
    @Operation(
        summary = "Get inventory items by product ID",
        description = "Retrieves all inventory items associated with a specific product."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved inventory items",
            content = @Content(schema = @Schema(implementation = InventoryItemDto.class, type = "array"))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<InventoryItemDto>> getItemsByProductId(
            @Parameter(description = "Product ID to filter inventory items", required = true)
            @PathVariable Integer productId) {
        List<InventoryItem> items = inventoryItemService.getItemsByProductId(productId);
        List<InventoryItemDto> itemDtos = items.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(itemDtos);
    }

    /**
     * GET /api/v1/inventory/items/status/{status} : Get inventory items by status
     *
     * @param status The status to filter by (e.g., AVAILABLE, ALLOCATED, QUARANTINED)
     * @return List of inventory items with the specified status
     */
    @GetMapping("/status/{status}")
    @Operation(
        summary = "Get inventory items by status",
        description = "Retrieves all inventory items with a specific status."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved inventory items",
            content = @Content(schema = @Schema(implementation = InventoryItemDto.class, type = "array"))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid status value"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<InventoryItemDto>> getItemsByStatus(
            @Parameter(description = "Status to filter inventory items (e.g., AVAILABLE, ALLOCATED, QUARANTINED)", 
                      required = true)
            @PathVariable String status) {
        List<InventoryItem> items = inventoryItemService.getItemsByStatus(status);
        List<InventoryItemDto> itemDtos = items.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(itemDtos);
    }

    /**
     * GET /api/v1/inventory/items/quantity/greater-than/{quantity} : Get inventory items with quantity greater than
     *
     * @param quantity The minimum quantity threshold
     * @return List of inventory items with quantity greater than the specified value
     */
    @GetMapping("/quantity/on-hand/greater-than/{quantity}")
    @Operation(
        summary = "Get inventory items with quantity on hand greater than specified value",
        description = "Retrieves all inventory items with a quantity on hand greater than the specified value."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved inventory items",
            content = @Content(schema = @Schema(implementation = InventoryItemDto.class, type = "array"))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid quantity value"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<InventoryItemDto>> getItemsByQuantityOnHandGreaterThan(
            @Parameter(description = "Minimum quantity threshold", required = true)
            @PathVariable BigDecimal quantity) {
        List<InventoryItem> items = inventoryItemService.getItemsByQuantityOnHandGreaterThan(quantity);
        List<InventoryItemDto> itemDtos = items.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(itemDtos);
    }

    // Helper methods for conversion between Entity and DTO
    private InventoryItemDto convertToDto(InventoryItem item) {
        InventoryItemDto dto = new InventoryItemDto();
        dto.setItemId(item.getItemId());
        dto.setTenantId(item.getTenantId());
        dto.setProductId(item.getProductId());
        dto.setVariantId(item.getVariantId());
        dto.setLotNumber(item.getLotNumber());
        dto.setSerialNumber(item.getSerialNumber());
        dto.setStatus(item.getStatus());
        dto.setCondition(item.getCondition());
        dto.setQuantityOnHand(item.getQuantityOnHand());
        dto.setQuantityAllocated(item.getQuantityAllocated());
        dto.setQuantityAvailable(item.getQuantityAvailable());
        dto.setUnitOfMeasure(item.getUnitOfMeasure());
        dto.setLocationId(item.getLocationId());
        dto.setFacilityId(item.getFacilityId());
        dto.setExpiryDate(item.getExpiryDate());
        dto.setManufactureDate(item.getManufactureDate());
        dto.setReceivedDate(item.getReceivedDate());
        dto.setLastCountedDate(item.getLastCountedDate());
        dto.setUnitCost(item.getUnitCost());
        dto.setTotalCost(item.getTotalCost());
        dto.setNotes(item.getNotes());
        dto.setIsActive(item.getIsActive());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());
        dto.setCreatedBy(item.getCreatedBy());
        dto.setUpdatedBy(item.getUpdatedBy());
        dto.setIsDeleted(item.getIsDeleted());
        return dto;
    }

    private InventoryItem convertToEntity(InventoryItemDto dto) {
        InventoryItem item = new InventoryItem();
        item.setItemId(dto.getItemId());
        item.setTenantId(dto.getTenantId());
        item.setProductId(dto.getProductId());
        item.setVariantId(dto.getVariantId());
        item.setLotNumber(dto.getLotNumber());
        item.setSerialNumber(dto.getSerialNumber());
        item.setStatus(dto.getStatus());
        item.setCondition(dto.getCondition());
        item.setQuantityOnHand(dto.getQuantityOnHand());
        item.setQuantityAllocated(dto.getQuantityAllocated());
        item.setQuantityAvailable(dto.getQuantityAvailable());
        item.setUnitOfMeasure(dto.getUnitOfMeasure());
        item.setLocationId(dto.getLocationId());
        item.setFacilityId(dto.getFacilityId());
        item.setExpiryDate(dto.getExpiryDate());
        item.setManufactureDate(dto.getManufactureDate());
        item.setReceivedDate(dto.getReceivedDate());
        item.setLastCountedDate(dto.getLastCountedDate());
        item.setUnitCost(dto.getUnitCost());
        item.setTotalCost(dto.getTotalCost());
        item.setNotes(dto.getNotes());
        item.setIsActive(dto.getIsActive());
        item.setCreatedAt(dto.getCreatedAt());
        item.setUpdatedAt(dto.getUpdatedAt());
        item.setCreatedBy(dto.getCreatedBy());
        item.setUpdatedBy(dto.getUpdatedBy());
        item.setIsDeleted(dto.getIsDeleted());
        return item;
    }
}
