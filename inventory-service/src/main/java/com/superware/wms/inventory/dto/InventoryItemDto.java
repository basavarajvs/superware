package com.superware.wms.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Inventory item data transfer object")
public class InventoryItemDto {

    @Schema(description = "Unique identifier of the inventory item", example = "1")
    private Integer itemId;

    @Schema(description = "Identifier of the tenant", example = "1")
    private Integer tenantId;

    @NotNull(message = "Product ID is required")
    @Schema(description = "Identifier of the product", example = "100")
    private Integer productId;

    @Schema(description = "Identifier of the product variant", example = "10")
    private Integer variantId;

    @Schema(description = "Lot number of the inventory item", example = "LOT123456")
    private String lotNumber;

    @Schema(description = "Serial number of the inventory item", example = "SN789012")
    private String serialNumber;

    @NotBlank(message = "Status is required")
    @Schema(description = "Status of the inventory item (e.g., AVAILABLE, ALLOCATED, QUARANTINED)", example = "AVAILABLE")
    private String status;

    @Schema(description = "Condition of the inventory item", example = "NEW")
    private String condition;

    @NotNull(message = "Quantity on hand is required")
    @PositiveOrZero(message = "Quantity on hand must be zero or positive")
    @Schema(description = "Quantity on hand", example = "100.00")
    private BigDecimal quantityOnHand;

    @NotNull(message = "Quantity allocated is required")
    @PositiveOrZero(message = "Quantity allocated must be zero or positive")
    @Schema(description = "Quantity allocated", example = "25.00")
    private BigDecimal quantityAllocated;

    @NotNull(message = "Quantity available is required")
    @PositiveOrZero(message = "Quantity available must be zero or positive")
    @Schema(description = "Quantity available", example = "75.00")
    private BigDecimal quantityAvailable;

    @NotBlank(message = "Unit of measure is required")
    @Schema(description = "Unit of measure", example = "EA")
    private String unitOfMeasure;

    @NotNull(message = "Location ID is required")
    @Schema(description = "Identifier of the location", example = "5")
    private Integer locationId;

    @NotNull(message = "Facility ID is required")
    @Schema(description = "Identifier of the facility", example = "3")
    private Integer facilityId;

    @Schema(description = "Expiry date of the inventory item")
    private LocalDateTime expiryDate;

    @Schema(description = "Manufacture date of the inventory item")
    private LocalDateTime manufactureDate;

    @Schema(description = "Received date of the inventory item")
    private LocalDateTime receivedDate;

    @Schema(description = "Last counted date of the inventory item")
    private LocalDateTime lastCountedDate;

    @NotNull(message = "Unit cost is required")
    @PositiveOrZero(message = "Unit cost must be zero or positive")
    @Schema(description = "Unit cost", example = "10.50")
    private BigDecimal unitCost;

    @NotNull(message = "Total cost is required")
    @PositiveOrZero(message = "Total cost must be zero or positive")
    @Schema(description = "Total cost", example = "1050.00")
    private BigDecimal totalCost;

    @Schema(description = "Notes about the inventory item")
    private String notes;

    @Schema(description = "Whether the inventory item is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "ID of the user who created the inventory item", example = "1")
    private Integer createdBy;

    @Schema(description = "ID of the user who last updated the inventory item", example = "1")
    private Integer updatedBy;

    @Schema(description = "Whether the inventory item is deleted", example = "false")
    private Boolean isDeleted;

    // Constructors
    public InventoryItemDto() {
    }

    // Getters and Setters
    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getVariantId() {
        return variantId;
    }

    public void setVariantId(Integer variantId) {
        this.variantId = variantId;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public BigDecimal getQuantityOnHand() {
        return quantityOnHand;
    }

    public void setQuantityOnHand(BigDecimal quantityOnHand) {
        this.quantityOnHand = quantityOnHand;
    }

    public BigDecimal getQuantityAllocated() {
        return quantityAllocated;
    }

    public void setQuantityAllocated(BigDecimal quantityAllocated) {
        this.quantityAllocated = quantityAllocated;
    }

    public BigDecimal getQuantityAvailable() {
        return quantityAvailable;
    }

    public void setQuantityAvailable(BigDecimal quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public Integer getLocationId() {
        return locationId;
    }

    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }

    public Integer getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(Integer facilityId) {
        this.facilityId = facilityId;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public LocalDateTime getManufactureDate() {
        return manufactureDate;
    }

    public void setManufactureDate(LocalDateTime manufactureDate) {
        this.manufactureDate = manufactureDate;
    }

    public LocalDateTime getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(LocalDateTime receivedDate) {
        this.receivedDate = receivedDate;
    }

    public LocalDateTime getLastCountedDate() {
        return lastCountedDate;
    }

    public void setLastCountedDate(LocalDateTime lastCountedDate) {
        this.lastCountedDate = lastCountedDate;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Integer updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InventoryItemDto that = (InventoryItemDto) o;

        return itemId != null ? itemId.equals(that.itemId) : that.itemId == null;
    }

    @Override
    public int hashCode() {
        return itemId != null ? itemId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "InventoryItemDto{" +
                "itemId=" + itemId +
                ", tenantId=" + tenantId +
                ", productId=" + productId +
                ", variantId=" + variantId +
                ", lotNumber='" + lotNumber + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", status='" + status + '\'' +
                ", condition='" + condition + '\'' +
                ", quantityOnHand=" + quantityOnHand +
                ", quantityAllocated=" + quantityAllocated +
                ", quantityAvailable=" + quantityAvailable +
                ", unitOfMeasure='" + unitOfMeasure + '\'' +
                ", locationId=" + locationId +
                ", facilityId=" + facilityId +
                ", expiryDate=" + expiryDate +
                ", manufactureDate=" + manufactureDate +
                ", receivedDate=" + receivedDate +
                ", lastCountedDate=" + lastCountedDate +
                ", unitCost=" + unitCost +
                ", totalCost=" + totalCost +
                ", notes='" + notes + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", createdBy=" + createdBy +
                ", updatedBy=" + updatedBy +
                ", isDeleted=" + isDeleted +
                '}';
    }
}