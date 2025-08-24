package com.superware.wms.inventory.entity;

import org.hibernate.annotations.Filter;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Inventory allocations entity.
 */
@Entity
@Table(name = "inventory_allocations")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class InventoryAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "allocation_id")
    private Integer allocationId;

    @Column(name = "tenant_id")
    private Integer tenantId;

    @Column(name = "reservation_detail_id")
    private Integer reservationDetailId;

    @Column(name = "item_id")
    private Integer itemId;

    @Column(name = "location_id")
    private Integer locationId;

    @Column(name = "lot_number")
    private String lotNumber;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "quantity_allocated")
    private BigDecimal quantityAllocated;

    @Column(name = "quantity_fulfilled")
    private BigDecimal quantityFulfilled;

    @Column(name = "unit_of_measure")
    private String unitOfMeasure;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

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
    public InventoryAllocation() {}

    // Getters and setters
    public Integer getAllocationId() {
        return allocationId;
    }

    public void setAllocationId(Integer allocationId) {
        this.allocationId = allocationId;
    }

    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public Integer getReservationDetailId() {
        return reservationDetailId;
    }

    public void setReservationDetailId(Integer reservationDetailId) {
        this.reservationDetailId = reservationDetailId;
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

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
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

        InventoryAllocation that = (InventoryAllocation) o;

        return allocationId != null ? allocationId.equals(that.allocationId) : that.allocationId == null;
    }

    @Override
    public int hashCode() {
        return allocationId != null ? allocationId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "InventoryAllocation{" +
                "allocationId=" + allocationId +
                ", tenantId=" + tenantId +
                ", reservationDetailId=" + reservationDetailId +
                ", itemId=" + itemId +
                ", locationId=" + locationId +
                ", lotNumber='" + lotNumber + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", quantityAllocated=" + quantityAllocated +
                ", quantityFulfilled=" + quantityFulfilled +
                ", unitOfMeasure='" + unitOfMeasure + '\'' +
                ", expiryDate=" + expiryDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", createdBy=" + createdBy +
                ", updatedBy=" + updatedBy +
                ", isDeleted=" + isDeleted +
                '}';
    }
}