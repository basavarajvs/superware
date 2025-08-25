package com.superware.wms.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Inventory count detail data transfer object")
public class InventoryCountDetailDto {

    @Schema(description = "Unique identifier of the inventory count detail", example = "1")
    private Integer countDetailId;

    @NotNull(message = "Count ID is required")
    @Schema(description = "Identifier of the count", example = "1")
    private Integer countId;

    @NotNull(message = "Item ID is required")
    @Schema(description = "Identifier of the inventory item", example = "1")
    private Integer itemId;

    @Schema(description = "Expected quantity", example = "100.00")
    private BigDecimal expectedQuantity;

    @NotNull(message = "Counted quantity is required")
    @Schema(description = "Counted quantity", example = "95.00")
    private BigDecimal countedQuantity;

    @Schema(description = "Variance", example = "-5.00")
    private BigDecimal variance;

    @Schema(description = "Unit of measure", example = "EA")
    private String unitOfMeasure;

    @Schema(description = "Lot number", example = "LOT123456")
    private String lotNumber;

    @Schema(description = "Notes about the count detail")
    private String notes;

    @Schema(description = "Whether the item is recounted", example = "false")
    private Boolean isRecounted;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "ID of the user who created the count detail", example = "1")
    private Integer createdBy;

    @Schema(description = "ID of the user who last updated the count detail", example = "1")
    private Integer updatedBy;

    @Schema(description = "Whether the count detail is deleted", example = "false")
    private Boolean isDeleted;

    // Constructors
    public InventoryCountDetailDto() {
    }

    // Getters and Setters
    public Integer getCountDetailId() {
        return countDetailId;
    }

    public void setCountDetailId(Integer countDetailId) {
        this.countDetailId = countDetailId;
    }

    public Integer getCountId() {
        return countId;
    }

    public void setCountId(Integer countId) {
        this.countId = countId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getExpectedQuantity() {
        return expectedQuantity;
    }

    public void setExpectedQuantity(BigDecimal expectedQuantity) {
        this.expectedQuantity = expectedQuantity;
    }

    public BigDecimal getCountedQuantity() {
        return countedQuantity;
    }

    public void setCountedQuantity(BigDecimal countedQuantity) {
        this.countedQuantity = countedQuantity;
    }

    public BigDecimal getVariance() {
        return variance;
    }

    public void setVariance(BigDecimal variance) {
        this.variance = variance;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getIsRecounted() {
        return isRecounted;
    }

    public void setIsRecounted(Boolean isRecounted) {
        this.isRecounted = isRecounted;
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

        InventoryCountDetailDto that = (InventoryCountDetailDto) o;

        return countDetailId != null ? countDetailId.equals(that.countDetailId) : that.countDetailId == null;
    }

    @Override
    public int hashCode() {
        return countDetailId != null ? countDetailId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "InventoryCountDetailDto{" +
                "countDetailId=" + countDetailId +
                ", countId=" + countId +
                ", itemId=" + itemId +
                ", expectedQuantity=" + expectedQuantity +
                ", countedQuantity=" + countedQuantity +
                ", variance=" + variance +
                ", unitOfMeasure='" + unitOfMeasure + '\'' +
                ", lotNumber='" + lotNumber + '\'' +
                ", notes='" + notes + '\'' +
                ", isRecounted=" + isRecounted +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", createdBy=" + createdBy +
                ", updatedBy=" + updatedBy +
                ", isDeleted=" + isDeleted +
                '}';
    }
}