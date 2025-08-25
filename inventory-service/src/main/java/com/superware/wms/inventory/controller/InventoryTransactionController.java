package com.superware.wms.inventory.controller;

import com.superware.wms.inventory.dto.InventoryTransactionDto;
import com.superware.wms.inventory.dto.InventoryTransactionDetailDto;
import com.superware.wms.inventory.entity.InventoryTransaction;
import com.superware.wms.inventory.entity.InventoryTransactionDetail;
import com.superware.wms.inventory.service.InventoryTransactionService;
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
 * REST controller for managing inventory transactions.
 */
@RestController
@RequestMapping("/api/v1/inventory/transactions")
@Tag(name = "Inventory Transactions", description = "APIs for managing inventory transactions")
public class InventoryTransactionController {

    private final InventoryTransactionService inventoryTransactionService;

    @Autowired
    public InventoryTransactionController(InventoryTransactionService inventoryTransactionService) {
        this.inventoryTransactionService = inventoryTransactionService;
    }

    /**
     * GET /api/v1/inventory/transactions : Get all inventory transactions with pagination
     *
     * @param pageable Pagination and sorting parameters
     * @return Paginated list of inventory transactions
     */
    @GetMapping
    @Operation(
        summary = "Get all inventory transactions with pagination",
        description = "Retrieves a paginated list of all inventory transactions in the system."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved inventory transactions",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<InventoryTransactionDto>> getAllTransactions(
            @Parameter(description = "Pagination and sorting parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<InventoryTransaction> transactions = inventoryTransactionService.getAllTransactions(pageable);
        Page<InventoryTransactionDto> transactionDtos = transactions.map(this::convertToDto);
        return ResponseEntity.ok(transactionDtos);
    }

    /**
     * GET /api/v1/inventory/transactions/{id} : Get an inventory transaction by ID
     *
     * @param id The ID of the inventory transaction to retrieve
     * @return The requested inventory transaction
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get an inventory transaction by ID",
        description = "Retrieves a specific inventory transaction by its unique identifier."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved the inventory transaction",
            content = @Content(schema = @Schema(implementation = InventoryTransactionDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "Inventory transaction not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InventoryTransactionDto> getTransactionById(
            @Parameter(description = "ID of the inventory transaction to be retrieved", required = true)
            @PathVariable Integer id) {
        InventoryTransaction transaction = inventoryTransactionService.getTransactionById(id);
        return ResponseEntity.ok(convertToDto(transaction));
    }

    /**
     * POST /api/v1/inventory/transactions : Create a new inventory transaction
     *
     * @param transactionDto The inventory transaction to create
     * @return The created inventory transaction
     */
    @PostMapping
    @Operation(
        summary = "Create a new inventory transaction",
        description = "Creates a new inventory transaction with the provided details."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Inventory transaction created successfully",
            content = @Content(schema = @Schema(implementation = InventoryTransactionDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InventoryTransactionDto> createTransaction(
            @Parameter(description = "Inventory transaction to be created", required = true)
            @Valid @RequestBody InventoryTransactionDto transactionDto) {
        InventoryTransaction transaction = convertToEntity(transactionDto);
        InventoryTransaction createdTransaction = inventoryTransactionService.createTransaction(transaction);
        
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(createdTransaction.getTransactionId())
            .toUri();
            
        return ResponseEntity.created(location).body(convertToDto(createdTransaction));
    }

    /**
     * PUT /api/v1/inventory/transactions/{id} : Update an existing inventory transaction
     *
     * @param id The ID of the inventory transaction to update
     * @param transactionDto The updated inventory transaction data
     * @return The updated inventory transaction
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update an existing inventory transaction",
        description = "Updates an existing inventory transaction with the provided details."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Inventory transaction updated successfully",
            content = @Content(schema = @Schema(implementation = InventoryTransactionDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Inventory transaction not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InventoryTransactionDto> updateTransaction(
            @Parameter(description = "ID of the inventory transaction to be updated", required = true)
            @PathVariable Integer id,
            @Parameter(description = "Updated inventory transaction data", required = true)
            @Valid @RequestBody InventoryTransactionDto transactionDto) {
        InventoryTransaction transaction = convertToEntity(transactionDto);
        InventoryTransaction updatedTransaction = inventoryTransactionService.updateTransaction(id, transaction);
        return ResponseEntity.ok(convertToDto(updatedTransaction));
    }

    /**
     * DELETE /api/v1/inventory/transactions/{id} : Delete an inventory transaction
     *
     * @param id The ID of the inventory transaction to delete
     * @return No content
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete an inventory transaction",
        description = "Deletes an inventory transaction by its unique identifier."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Inventory transaction deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Inventory transaction not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deleteTransaction(
            @Parameter(description = "ID of the inventory transaction to be deleted", required = true)
            @PathVariable Integer id) {
        inventoryTransactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/inventory/transactions/{transactionId}/details : Get transaction details by transaction ID
     *
     * @param transactionId The ID of the transaction
     * @return List of inventory transaction details
     */
    @GetMapping("/{transactionId}/details")
    @Operation(
        summary = "Get transaction details by transaction ID",
        description = "Retrieves all inventory transaction details for a specific transaction."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved inventory transaction details",
            content = @Content(schema = @Schema(implementation = InventoryTransactionDetailDto.class, type = "array"))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<InventoryTransactionDetailDto>> getTransactionDetailsByTransactionId(
            @Parameter(description = "ID of the transaction", required = true)
            @PathVariable Integer transactionId) {
        List<InventoryTransactionDetail> details = inventoryTransactionService.getTransactionDetailsByTransactionId(transactionId);
        List<InventoryTransactionDetailDto> detailDtos = details.stream()
                .map(this::convertDetailToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(detailDtos);
    }

    /**
     * POST /api/v1/inventory/transactions/receipt : Record a stock receipt transaction
     *
     * @param itemId The ID of the inventory item
     * @param quantity The quantity received
     * @param fromLocationId The ID of the source location
     * @param toLocationId The ID of the destination location
     * @param userId The ID of the user performing the transaction
     * @return The created inventory transaction
     */
    @PostMapping("/receipt")
    @Operation(
        summary = "Record a stock receipt transaction",
        description = "Records a stock receipt transaction."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Stock receipt recorded successfully",
            content = @Content(schema = @Schema(implementation = InventoryTransactionDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Inventory item not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InventoryTransactionDto> recordReceipt(
            @Parameter(description = "ID of the inventory item", required = true)
            @RequestParam Integer itemId,
            @Parameter(description = "Quantity received", required = true)
            @RequestParam BigDecimal quantity,
            @Parameter(description = "ID of the source location", required = true)
            @RequestParam Integer fromLocationId,
            @Parameter(description = "ID of the destination location", required = true)
            @RequestParam Integer toLocationId,
            @Parameter(description = "ID of the user performing the transaction", required = true)
            @RequestParam Integer userId) {
        InventoryTransaction transaction = inventoryTransactionService.recordReceipt(itemId, quantity, fromLocationId, toLocationId, userId);
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(transaction.getTransactionId())
            .toUri();
        return ResponseEntity.created(location).body(convertToDto(transaction));
    }

    /**
     * POST /api/v1/inventory/transactions/issue : Record a stock issue transaction
     *
     * @param itemId The ID of the inventory item
     * @param quantity The quantity issued
     * @param fromLocationId The ID of the source location
     * @param toLocationId The ID of the destination location
     * @param userId The ID of the user performing the transaction
     * @return The created inventory transaction
     */
    @PostMapping("/issue")
    @Operation(
        summary = "Record a stock issue transaction",
        description = "Records a stock issue transaction."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Stock issue recorded successfully",
            content = @Content(schema = @Schema(implementation = InventoryTransactionDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Inventory item not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InventoryTransactionDto> recordIssue(
            @Parameter(description = "ID of the inventory item", required = true)
            @RequestParam Integer itemId,
            @Parameter(description = "Quantity issued", required = true)
            @RequestParam BigDecimal quantity,
            @Parameter(description = "ID of the source location", required = true)
            @RequestParam Integer fromLocationId,
            @Parameter(description = "ID of the destination location", required = true)
            @RequestParam Integer toLocationId,
            @Parameter(description = "ID of the user performing the transaction", required = true)
            @RequestParam Integer userId) {
        InventoryTransaction transaction = inventoryTransactionService.recordIssue(itemId, quantity, fromLocationId, toLocationId, userId);
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(transaction.getTransactionId())
            .toUri();
        return ResponseEntity.created(location).body(convertToDto(transaction));
    }

    /**
     * POST /api/v1/inventory/transactions/transfer : Record a stock transfer transaction
     *
     * @param itemId The ID of the inventory item
     * @param quantity The quantity transferred
     * @param fromLocationId The ID of the source location
     * @param toLocationId The ID of the destination location
     * @param userId The ID of the user performing the transaction
     * @return The created inventory transaction
     */
    @PostMapping("/transfer")
    @Operation(
        summary = "Record a stock transfer transaction",
        description = "Records a stock transfer transaction."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Stock transfer recorded successfully",
            content = @Content(schema = @Schema(implementation = InventoryTransactionDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Inventory item not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InventoryTransactionDto> recordTransfer(
            @Parameter(description = "ID of the inventory item", required = true)
            @RequestParam Integer itemId,
            @Parameter(description = "Quantity transferred", required = true)
            @RequestParam BigDecimal quantity,
            @Parameter(description = "ID of the source location", required = true)
            @RequestParam Integer fromLocationId,
            @Parameter(description = "ID of the destination location", required = true)
            @RequestParam Integer toLocationId,
            @Parameter(description = "ID of the user performing the transaction", required = true)
            @RequestParam Integer userId) {
        InventoryTransaction transaction = inventoryTransactionService.recordTransfer(itemId, quantity, fromLocationId, toLocationId, userId);
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(transaction.getTransactionId())
            .toUri();
        return ResponseEntity.created(location).body(convertToDto(transaction));
    }

    // Helper methods for conversion between Entity and DTO
    private InventoryTransactionDto convertToDto(InventoryTransaction transaction) {
        InventoryTransactionDto dto = new InventoryTransactionDto();
        dto.setTransactionId(transaction.getTransactionId());
        dto.setTenantId(transaction.getTenantId());
        dto.setTransactionType(transaction.getTransactionType());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setStatus(transaction.getStatus());
        dto.setReferenceNumber(transaction.getReferenceNumber());
        dto.setReferenceType(transaction.getReferenceType());
        dto.setReferenceId(transaction.getReferenceId());
        dto.setSourceType(transaction.getSourceType());
        dto.setSourceId(transaction.getSourceId());
        dto.setDestinationType(transaction.getDestinationType());
        dto.setDestinationId(transaction.getDestinationId());
        dto.setNotes(transaction.getNotes());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setUpdatedAt(transaction.getUpdatedAt());
        dto.setCreatedBy(transaction.getCreatedBy());
        dto.setUpdatedBy(transaction.getUpdatedBy());
        dto.setIsDeleted(transaction.getIsDeleted());
        return dto;
    }

    private InventoryTransaction convertToEntity(InventoryTransactionDto dto) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setTransactionId(dto.getTransactionId());
        transaction.setTenantId(dto.getTenantId());
        transaction.setTransactionType(dto.getTransactionType());
        transaction.setTransactionDate(dto.getTransactionDate());
        transaction.setStatus(dto.getStatus());
        transaction.setReferenceNumber(dto.getReferenceNumber());
        transaction.setReferenceType(dto.getReferenceType());
        transaction.setReferenceId(dto.getReferenceId());
        transaction.setSourceType(dto.getSourceType());
        transaction.setSourceId(dto.getSourceId());
        transaction.setDestinationType(dto.getDestinationType());
        transaction.setDestinationId(dto.getDestinationId());
        transaction.setNotes(dto.getNotes());
        transaction.setCreatedAt(dto.getCreatedAt());
        transaction.setUpdatedAt(dto.getUpdatedAt());
        transaction.setCreatedBy(dto.getCreatedBy());
        transaction.setUpdatedBy(dto.getUpdatedBy());
        transaction.setIsDeleted(dto.getIsDeleted());
        return transaction;
    }

    private InventoryTransactionDetailDto convertDetailToDto(InventoryTransactionDetail detail) {
        InventoryTransactionDetailDto dto = new InventoryTransactionDetailDto();
        dto.setTransactionDetailId(detail.getTransactionDetailId());
        dto.setTransactionId(detail.getTransactionId());
        dto.setItemId(detail.getItemId());
        dto.setQuantity(detail.getQuantity());
        dto.setUnitOfMeasure(detail.getUnitOfMeasure());
        dto.setUnitCost(detail.getUnitCost());
        dto.setTotalCost(detail.getTotalCost());
        dto.setLotNumber(detail.getLotNumber());
        dto.setSerialNumber(detail.getSerialNumber());
        dto.setFromLocationId(detail.getFromLocationId());
        dto.setToLocationId(detail.getToLocationId());
        dto.setNotes(detail.getNotes());
        dto.setCreatedAt(detail.getCreatedAt());
        dto.setUpdatedAt(detail.getUpdatedAt());
        dto.setCreatedBy(detail.getCreatedBy());
        dto.setUpdatedBy(detail.getUpdatedBy());
        dto.setIsDeleted(detail.getIsDeleted());
        return dto;
    }
}