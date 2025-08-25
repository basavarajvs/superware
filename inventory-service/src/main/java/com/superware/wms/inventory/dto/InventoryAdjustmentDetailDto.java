package com.superware.wms.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Inventory adjustment detail data transfer object")
public class InventoryAdjustmentDetailDto {

    @Schema(description = "Unique identifier of the inventory adjustment detail", example = "1")
    private Integer adjustmentDetailId;

    @NotNull(message = "Adjustment ID is required")
    @Schema(description = "Identifier of the adjustment", example = "1")
    private Integer adjustmentId;

    @NotNull(message = "Item ID is required")
    @Schema(description = "Identifier of the inventory item", example = "1")
    private Integer itemId;

    @Schema(description = "Identifier of the location", example = "5")
    private Integer locationId;

    @Schema(description = "Lot number", example = "LOT123456")
    private String lotNumber;

    @Schema(description = "Serial number", example = "SN789012")
    private String serialNumber;

    @Schema(description = "Quantity before adjustment", example = "100.00")
    private BigDecimal quantityBefore;

    @Schema(description = "Quantity after adjustment", example = "125.00")
    private BigDecimal quantityAfter;

    @NotNull(message = "Quantity adjusted is required")
    @Schema(description = "Quantity adjusted", example = "25.00")
    private BigDecimal quantityAdjusted;

    @Schema(description = "Unit of measure", example = "EA")
    private String unitOfMeasure;

    @Schema(description = "Unit cost", example = "10.50")
    private BigDecimal unitCost;

    @Schema(description = "Total cost", example = "262.50")
    private BigDecimal totalCost;

    @Schema(description = "Notes about the adjustment detail")
    private String notes;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "ID of the user who created the adjustment detail", example = "1")
    private Integer createdBy;

    @Schema(description = "ID of the user who last updated the adjustment detail", example = "1")
    private Integer updatedBy;

    @Schema(description = "Whether the adjustment detail is deleted", example = "false")
    private Boolean isDeleted;

    // Constructors
    public InventoryAdjustmentDetailDto() {
    }

    // Getters and Setters
    public Integer getAdjustmentDetailId() {
        return adjustmentDetailId;
    }

    public void setAdjustmentDetailId(Integer adjustmentDetailId) {
        this.adjustmentDetailId = adjustmentDetailId;
    }

    public Integer getAdjustmentId() {
        return adjustmentId;
    }

    public void setAdjustmentId(Integer adjustmentId) {
        this.adjustmentId = adjustmentId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getLocationId() {
        return locationId;
    }

    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
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

    public BigDecimal getQuantityBefore() {
        return quantityBefore;
    }

    public void setQuantityBefore(BigDecimal quantityBefore) {
        this.quantityBefore = quantityBefore;
    }

    public BigDecimal getQuantityAfter() {
        return quantityAfter;
    }

    public void setQuantityAfter(BigDecimal quantityAfter) {
        this.quantityAfter = quantityAfter;
    }

    public BigDecimal getQuantityAdjusted() {
        return quantityAdjusted;
    }

    public void setQuantityAdjusted(BigDecimal quantityAdjusted) {
        this.quantityAdjusted = quantityAdjusted;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
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

        InventoryAdjustmentDetailDto that = (InventoryAdjustmentDetailDto) o;

        return adjustmentDetailId != null ? adjustmentDetailId.equals(that.adjustmentDetailId) : that.adjustmentDetailId == null;
    }

    @Override
    public int hashCode() {
        return adjustmentDetailId != null ? adjustmentDetailId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "InventoryAdjustmentDetailDto{" +
                "adjustmentDetailId=" + adjustmentDetailId +
                ", adjustmentId=" + adjustmentId +
                ", itemId=" + itemId +
                ", locationId=" + locationId +
                ", lotNumber='" + lotNumber + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", quantityBefore=" + quantityBefore +
                ", quantityAfter=" + quantityAfter +
                ", quantityAdjusted=" + quantityAdjusted +
                ", unitOfMeasure='" + unitOfMeasure + '\'' +
                ", unitCost=" + unitCost +
                ", totalCost=" + totalCost +
                ", notes='" + notes + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", createdBy=" + createdBy +
                ", updatedBy=" + updatedBy +
                ", isDeleted=" + isDeleted +
                '}';
    }
}