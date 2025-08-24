package com.superware.wms.inventory.entity;

import org.hibernate.annotations.Filter;

import org.hibernate.annotations.Filter;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Inventory reservation details entity.
 */
@Entity
@Table(name = "inventory_reservation_details")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class InventoryReservationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_detail_id")
    private Integer reservationDetailId;

    @Column(name = "reservation_id")
    private Integer reservationId;

    @Column(name = "item_id")
    private Integer itemId;

    @Column(name = "quantity_requested")
    private BigDecimal quantityRequested;

    @Column(name = "quantity_allocated")
    private BigDecimal quantityAllocated;

    @Column(name = "quantity_fulfilled")
    private BigDecimal quantityFulfilled;

    @Column(name = "unit_of_measure")
    private String unitOfMeasure;

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
    public InventoryReservationDetail() {}

    // Getters and setters
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

        InventoryReservationDetail that = (InventoryReservationDetail) o;

        return reservationDetailId != null ? reservationDetailId.equals(that.reservationDetailId) : that.reservationDetailId == null;
    }

    @Override
    public int hashCode() {
        return reservationDetailId != null ? reservationDetailId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "InventoryReservationDetail{" +
                "reservationDetailId=" + reservationDetailId +
                ", reservationId=" + reservationId +
                ", itemId=" + itemId +
                ", quantityRequested=" + quantityRequested +
                ", quantityAllocated=" + quantityAllocated +
                ", quantityFulfilled=" + quantityFulfilled +
                ", unitOfMeasure='" + unitOfMeasure + '\'' +
                ", notes='" + notes + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", createdBy=" + createdBy +
                ", updatedBy=" + updatedBy +
                ", isDeleted=" + isDeleted +
                '}';
    }
}