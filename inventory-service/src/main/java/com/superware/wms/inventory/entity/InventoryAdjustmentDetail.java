package com.superware.wms.inventory.entity;

import org.hibernate.annotations.Filter;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Inventory adjustment details entity.
 */
@Entity
@Table(name = "inventory_adjustment_details")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class InventoryAdjustmentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "adjustment_detail_id")
    private Integer adjustmentDetailId;

    @Column(name = "adjustment_id")
    private Integer adjustmentId;

    @Column(name = "item_id")
    private Integer itemId;

    @Column(name = "location_id")
    private Integer locationId;

    @Column(name = "lot_number")
    private String lotNumber;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "quantity_before")
    private BigDecimal quantityBefore;

    @Column(name = "quantity_after")
    private BigDecimal quantityAfter;

    @Column(name = "quantity_adjusted")
    private BigDecimal quantityAdjusted;

    @Column(name = "unit_of_measure")
    private String unitOfMeasure;

    @Column(name = "unit_cost")
    private BigDecimal unitCost;

    @Column(name = "total_cost")
    private BigDecimal totalCost;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    // Constructors
    public InventoryAdjustmentDetail() {}

    // Getters and setters
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

        InventoryAdjustmentDetail that = (InventoryAdjustmentDetail) o;

        return adjustmentDetailId != null ? adjustmentDetailId.equals(that.adjustmentDetailId) : that.adjustmentDetailId == null;
    }

    @Override
    public int hashCode() {
        return adjustmentDetailId != null ? adjustmentDetailId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "InventoryAdjustmentDetail{" +
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