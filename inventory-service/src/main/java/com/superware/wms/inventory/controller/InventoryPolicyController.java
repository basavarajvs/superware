package com.superware.wms.inventory.controller;

import com.superware.wms.inventory.dto.InventoryPolicyDto;
import com.superware.wms.inventory.entity.InventoryPolicy;
import com.superware.wms.inventory.service.InventoryPolicyService;
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
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing inventory policies.
 */
@RestController
@RequestMapping("/api/v1/inventory/policies")
@Tag(name = "Inventory Policies", description = "APIs for managing inventory policies")
public class InventoryPolicyController {

    private final InventoryPolicyService inventoryPolicyService;

    @Autowired
    public InventoryPolicyController(InventoryPolicyService inventoryPolicyService) {
        this.inventoryPolicyService = inventoryPolicyService;
    }

    /**
     * GET /api/v1/inventory/policies : Get all inventory policies with pagination
     *
     * @param pageable Pagination and sorting parameters
     * @return Paginated list of inventory policies
     */
    @GetMapping
    @Operation(
        summary = "Get all inventory policies with pagination",
        description = "Retrieves a paginated list of all inventory policies in the system."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved inventory policies",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<InventoryPolicyDto>> getAllPolicies(
            @Parameter(description = "Pagination and sorting parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<InventoryPolicy> policies = inventoryPolicyService.getAllPolicies(pageable);
        Page<InventoryPolicyDto> policyDtos = policies.map(this::convertToDto);
        return ResponseEntity.ok(policyDtos);
    }

    /**
     * GET /api/v1/inventory/policies/{id} : Get an inventory policy by ID
     *
     * @param id The ID of the inventory policy to retrieve
     * @return The requested inventory policy
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get an inventory policy by ID",
        description = "Retrieves a specific inventory policy by its unique identifier."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved the inventory policy",
            content = @Content(schema = @Schema(implementation = InventoryPolicyDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "Inventory policy not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InventoryPolicyDto> getPolicyById(
            @Parameter(description = "ID of the inventory policy to be retrieved", required = true)
            @PathVariable Integer id) {
        InventoryPolicy policy = inventoryPolicyService.getPolicyById(id);
        return ResponseEntity.ok(convertToDto(policy));
    }

    /**
     * POST /api/v1/inventory/policies : Create a new inventory policy
     *
     * @param policyDto The inventory policy to create
     * @return The created inventory policy
     */
    @PostMapping
    @Operation(
        summary = "Create a new inventory policy",
        description = "Creates a new inventory policy with the provided details."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Inventory policy created successfully",
            content = @Content(schema = @Schema(implementation = InventoryPolicyDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InventoryPolicyDto> createPolicy(
            @Parameter(description = "Inventory policy to be created", required = true)
            @Valid @RequestBody InventoryPolicyDto policyDto) {
        InventoryPolicy policy = convertToEntity(policyDto);
        InventoryPolicy createdPolicy = inventoryPolicyService.createPolicy(policy);
        
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(createdPolicy.getPolicyId())
            .toUri();
            
        return ResponseEntity.created(location).body(convertToDto(createdPolicy));
    }

    /**
     * PUT /api/v1/inventory/policies/{id} : Update an existing inventory policy
     *
     * @param id The ID of the inventory policy to update
     * @param policyDto The updated inventory policy data
     * @return The updated inventory policy
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update an existing inventory policy",
        description = "Updates an existing inventory policy with the provided details."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Inventory policy updated successfully",
            content = @Content(schema = @Schema(implementation = InventoryPolicyDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Inventory policy not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<InventoryPolicyDto> updatePolicy(
            @Parameter(description = "ID of the inventory policy to be updated", required = true)
            @PathVariable Integer id,
            @Parameter(description = "Updated inventory policy data", required = true)
            @Valid @RequestBody InventoryPolicyDto policyDto) {
        InventoryPolicy policy = convertToEntity(policyDto);
        InventoryPolicy updatedPolicy = inventoryPolicyService.updatePolicy(id, policy);
        return ResponseEntity.ok(convertToDto(updatedPolicy));
    }

    /**
     * DELETE /api/v1/inventory/policies/{id} : Delete an inventory policy
     *
     * @param id The ID of the inventory policy to delete
     * @return No content
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete an inventory policy",
        description = "Deletes an inventory policy by its unique identifier."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Inventory policy deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Inventory policy not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deletePolicy(
            @Parameter(description = "ID of the inventory policy to be deleted", required = true)
            @PathVariable Integer id) {
        inventoryPolicyService.deletePolicy(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/inventory/policies/product/{productId} : Get inventory policies by product ID
     *
     * @param productId The ID of the product
     * @return List of inventory policies for the product
     */
    @GetMapping("/product/{productId}")
    @Operation(
        summary = "Get inventory policies by product ID",
        description = "Retrieves all inventory policies associated with a specific product."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved inventory policies",
            content = @Content(schema = @Schema(implementation = InventoryPolicyDto.class, type = "array"))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<InventoryPolicyDto>> getPoliciesByProductId(
            @Parameter(description = "Product ID to filter inventory policies", required = true)
            @PathVariable Integer productId) {
        List<InventoryPolicy> policies = inventoryPolicyService.getPoliciesByProductId(productId);
        List<InventoryPolicyDto> policyDtos = policies.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(policyDtos);
    }

    /**
     * GET /api/v1/inventory/policies/facility/{facilityId} : Get inventory policies by facility ID
     *
     * @param facilityId The ID of the facility
     * @return List of inventory policies for the facility
     */
    @GetMapping("/facility/{facilityId}")
    @Operation(
        summary = "Get inventory policies by facility ID",
        description = "Retrieves all inventory policies associated with a specific facility."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved inventory policies",
            content = @Content(schema = @Schema(implementation = InventoryPolicyDto.class, type = "array"))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<InventoryPolicyDto>> getPoliciesByFacilityId(
            @Parameter(description = "Facility ID to filter inventory policies", required = true)
            @PathVariable Integer facilityId) {
        List<InventoryPolicy> policies = inventoryPolicyService.getPoliciesByFacilityId(facilityId);
        List<InventoryPolicyDto> policyDtos = policies.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(policyDtos);
    }

    // Helper methods for conversion between Entity and DTO
    private InventoryPolicyDto convertToDto(InventoryPolicy policy) {
        InventoryPolicyDto dto = new InventoryPolicyDto();
        dto.setPolicyId(policy.getPolicyId());
        dto.setTenantId(policy.getTenantId());
        dto.setProductId(policy.getProductId());
        dto.setVariantId(policy.getVariantId());
        dto.setFacilityId(policy.getFacilityId());
        dto.setMinStockLevel(policy.getMinStockLevel());
        dto.setMaxStockLevel(policy.getMaxStockLevel());
        dto.setReorderPoint(policy.getReorderPoint());
        dto.setReorderQuantity(policy.getReorderQuantity());
        dto.setValuationMethod(policy.getValuationMethod());
        dto.setAbcClass(policy.getAbcClass());
        dto.setIsActive(policy.getIsActive());
        dto.setCreatedAt(policy.getCreatedAt());
        dto.setUpdatedAt(policy.getUpdatedAt());
        dto.setCreatedBy(policy.getCreatedBy());
        dto.setUpdatedBy(policy.getUpdatedBy());
        dto.setIsDeleted(policy.getIsDeleted());
        return dto;
    }

    private InventoryPolicy convertToEntity(InventoryPolicyDto dto) {
        InventoryPolicy policy = new InventoryPolicy();
        policy.setPolicyId(dto.getPolicyId());
        policy.setTenantId(dto.getTenantId());
        policy.setProductId(dto.getProductId());
        policy.setVariantId(dto.getVariantId());
        policy.setFacilityId(dto.getFacilityId());
        policy.setMinStockLevel(dto.getMinStockLevel());
        policy.setMaxStockLevel(dto.getMaxStockLevel());
        policy.setReorderPoint(dto.getReorderPoint());
        policy.setReorderQuantity(dto.getReorderQuantity());
        policy.setValuationMethod(dto.getValuationMethod());
        policy.setAbcClass(dto.getAbcClass());
        policy.setIsActive(dto.getIsActive());
        policy.setCreatedAt(dto.getCreatedAt());
        policy.setUpdatedAt(dto.getUpdatedAt());
        policy.setCreatedBy(dto.getCreatedBy());
        policy.setUpdatedBy(dto.getUpdatedBy());
        policy.setIsDeleted(dto.getIsDeleted());
        return policy;
    }
}