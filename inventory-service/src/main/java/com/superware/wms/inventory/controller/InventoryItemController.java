package com.superware.wms.inventory.controller;

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
    public ResponseEntity<Page<InventoryItem>> getAllItems(
            @Parameter(description = "Pagination and sorting parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(inventoryItemService.getAllItems(pageable));
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
            content = @Content(schema = @Schema(implementation = InventoryItem.class))
        ),
        @ApiResponse(responseCode = "404", description = "Inventory item not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InventoryItem> getItemById(
            @Parameter(description = "ID of the inventory item to be retrieved", required = true)
            @PathVariable Integer id) {
        return ResponseEntity.ok(inventoryItemService.getItemById(id));
    }

    /**
     * POST /api/v1/inventory/items : Create a new inventory item
     *
     * @param item The inventory item to create
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
            content = @Content(schema = @Schema(implementation = InventoryItem.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InventoryItem> createItem(
            @Parameter(description = "Inventory item to be created", required = true)
            @Valid @RequestBody InventoryItem item) {
        InventoryItem createdItem = inventoryItemService.createItem(item);
        
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(createdItem.getItemId())
            .toUri();
            
        return ResponseEntity.created(location).body(createdItem);
    }

    /**
     * PUT /api/v1/inventory/items/{id} : Update an existing inventory item
     *
     * @param id The ID of the inventory item to update
     * @param item The updated inventory item data
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
            content = @Content(schema = @Schema(implementation = InventoryItem.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Inventory item not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InventoryItem> updateItem(
            @Parameter(description = "ID of the inventory item to be updated", required = true)
            @PathVariable Integer id,
            @Parameter(description = "Updated inventory item data", required = true)
            @Valid @RequestBody InventoryItem item) {
        return ResponseEntity.ok(inventoryItemService.updateItem(id, item));
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
            content = @Content(schema = @Schema(implementation = InventoryItem.class, type = "array"))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Iterable<InventoryItem>> getItemsByProductId(
            @Parameter(description = "Product ID to filter inventory items", required = true)
            @PathVariable Integer productId) {
        return ResponseEntity.ok(inventoryItemService.getItemsByProductId(productId));
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
            content = @Content(schema = @Schema(implementation = InventoryItem.class, type = "array"))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid status value"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Iterable<InventoryItem>> getItemsByStatus(
            @Parameter(description = "Status to filter inventory items (e.g., AVAILABLE, ALLOCATED, QUARANTINED)", 
                      required = true)
            @PathVariable String status) {
        return ResponseEntity.ok(inventoryItemService.getItemsByStatus(status));
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
            content = @Content(schema = @Schema(implementation = InventoryItem.class, type = "array"))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid quantity value"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Iterable<InventoryItem>> getItemsByQuantityOnHandGreaterThan(
            @Parameter(description = "Minimum quantity threshold", required = true)
            @PathVariable BigDecimal quantity) {
        return ResponseEntity.ok(inventoryItemService.getItemsByQuantityOnHandGreaterThan(quantity));
    }
}
