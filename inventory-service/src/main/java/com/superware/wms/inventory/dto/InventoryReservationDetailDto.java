package com.superware.wms.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Inventory reservation detail data transfer object")
public class InventoryReservationDetailDto {

    @Schema(description = "Unique identifier of the inventory reservation detail", example = "1")
    private Integer reservationDetailId;

    @NotNull(message = "Reservation ID is required")
    @Schema(description = "Identifier of the reservation", example = "1")
    private Integer reservationId;

    @NotNull(message = "Item ID is required")
    @Schema(description = "Identifier of the inventory item", example = "1")
    private Integer itemId;

    @NotNull(message = "Quantity requested is required")
    @PositiveOrZero(message = "Quantity requested must be zero or positive")
    @Schema(description = "Quantity requested", example = "25.00")
    private BigDecimal quantityRequested;

    @Schema(description = "Quantity allocated", example = "20.00")
    private BigDecimal quantityAllocated;

    @Schema(description = "Quantity fulfilled", example = "15.00")
    private BigDecimal quantityFulfilled;

    @Schema(description = "Unit of measure", example = "EA")
    private String unitOfMeasure;

    @Schema(description = "Lot number", example = "LOT123456")
    private String lotNumber;

    @Schema(description = "Notes about the reservation detail")
    private String notes;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "ID of the user who created the reservation detail", example = "1")
    private Integer createdBy;

    @Schema(description = "ID of the user who last updated the reservation detail", example = "1")
    private Integer updatedBy;

    @Schema(description = "Whether the reservation detail is deleted", example = "false")
    private Boolean isDeleted;

    // Constructors
    public InventoryReservationDetailDto() {
    }

    // Getters and Setters
    public Integer getReservationDetailId() {
        return reservationDetailId;
    }

    public void setReservationDetailId(Integer reservationDetailId) {
        this.reservationDetailId = reservationDetailId;
    }

    public Integer getReservationId() {
        return reservationId;
    }

    public void setReservationId(Integer reservationId) {
        this.reservationId = reservationId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getQuantityRequested() {
        return quantityRequested;
    }

    public void setQuantityRequested(BigDecimal quantityRequested) {
        this.quantityRequested = quantityRequested;
    }

    public BigDecimal getQuantityAllocated() {
        return quantityAllocated;
    }

    public void setQuantityAllocated(BigDecimal quantityAllocated) {
        this.quantityAllocated = quantityAllocated;
    }

    public BigDecimal getQuantityFulfilled() {
        return quantityFulfilled;
    }

    public void setQuantityFulfilled(BigDecimal quantityFulfilled) {
        this.quantityFulfilled = quantityFulfilled;
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

        InventoryReservationDetailDto that = (InventoryReservationDetailDto) o;

        return reservationDetailId != null ? reservationDetailId.equals(that.reservationDetailId) : that.reservationDetailId == null;
    }

    @Override
    public int hashCode() {
        return reservationDetailId != null ? reservationDetailId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "InventoryReservationDetailDto{" +
                "reservationDetailId=" + reservationDetailId +
                ", reservationId=" + reservationId +
                ", itemId=" + itemId +
                ", quantityRequested=" + quantityRequested +
                ", quantityAllocated=" + quantityAllocated +
                ", quantityFulfilled=" + quantityFulfilled +
                ", unitOfMeasure='" + unitOfMeasure + '\'' +
                ", lotNumber='" + lotNumber + '\'' +
                ", notes='" + notes + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", createdBy=" + createdBy +
                ", updatedBy=" + updatedBy +
                ", isDeleted=" + isDeleted +
                '}';
    }
}