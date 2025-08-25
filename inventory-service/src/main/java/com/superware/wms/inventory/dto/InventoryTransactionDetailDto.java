package com.superware.wms.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Inventory transaction detail data transfer object")
public class InventoryTransactionDetailDto {

    @Schema(description = "Unique identifier of the inventory transaction detail", example = "1")
    private Integer transactionDetailId;

    @NotNull(message = "Transaction ID is required")
    @Schema(description = "Identifier of the transaction", example = "1")
    private Integer transactionId;

    @NotNull(message = "Item ID is required")
    @Schema(description = "Identifier of the inventory item", example = "1")
    private Integer itemId;

    @NotNull(message = "Quantity is required")
    @PositiveOrZero(message = "Quantity must be zero or positive")
    @Schema(description = "Quantity", example = "25.00")
    private BigDecimal quantity;

    @Schema(description = "Unit of measure", example = "EA")
    private String unitOfMeasure;

    @Schema(description = "Unit cost", example = "10.50")
    private BigDecimal unitCost;

    @Schema(description = "Total cost", example = "262.50")
    private BigDecimal totalCost;

    @Schema(description = "Lot number", example = "LOT123456")
    private String lotNumber;

    @Schema(description = "Serial number", example = "SN789012")
    private String serialNumber;

    @Schema(description = "From location ID", example = "5")
    private Integer fromLocationId;

    @Schema(description = "To location ID", example = "3")
    private Integer toLocationId;

    @Schema(description = "Notes about the transaction detail")
    private String notes;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "ID of the user who created the transaction detail", example = "1")
    private Integer createdBy;

    @Schema(description = "ID of the user who last updated the transaction detail", example = "1")
    private Integer updatedBy;

    @Schema(description = "Whether the transaction detail is deleted", example = "false")
    private Boolean isDeleted;

    // Constructors
    public InventoryTransactionDetailDto() {
    }

    // Getters and Setters
    public Integer getTransactionDetailId() {
        return transactionDetailId;
    }

    public void setTransactionDetailId(Integer transactionDetailId) {
        this.transactionDetailId = transactionDetailId;
    }

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
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

    public Integer getFromLocationId() {
        return fromLocationId;
    }

    public void setFromLocationId(Integer fromLocationId) {
        this.fromLocationId = fromLocationId;
    }

    public Integer getToLocationId() {
        return toLocationId;
    }

    public void setToLocationId(Integer toLocationId) {
        this.toLocationId = toLocationId;
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

        InventoryTransactionDetailDto that = (InventoryTransactionDetailDto) o;

        return transactionDetailId != null ? transactionDetailId.equals(that.transactionDetailId) : that.transactionDetailId == null;
    }

    @Override
    public int hashCode() {
        return transactionDetailId != null ? transactionDetailId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "InventoryTransactionDetailDto{" +
                "transactionDetailId=" + transactionDetailId +
                ", transactionId=" + transactionId +
                ", itemId=" + itemId +
                ", quantity=" + quantity +
                ", unitOfMeasure='" + unitOfMeasure + '\'' +
                ", unitCost=" + unitCost +
                ", totalCost=" + totalCost +
                ", lotNumber='" + lotNumber + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", fromLocationId=" + fromLocationId +
                ", toLocationId=" + toLocationId +
                ", notes='" + notes + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", createdBy=" + createdBy +
                ", updatedBy=" + updatedBy +
                ", isDeleted=" + isDeleted +
                '}';
    }
}