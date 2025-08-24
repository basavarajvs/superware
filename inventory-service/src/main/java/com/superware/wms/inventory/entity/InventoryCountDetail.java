package com.superware.wms.inventory.entity;

import org.hibernate.annotations.Filter;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Inventory count details entity.
 */
@Entity
@Table(name = "inventory_count_details")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class InventoryCountDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "count_detail_id")
    private Integer countDetailId;

    @Column(name = "count_id")
    private Integer countId;

    @Column(name = "item_id")
    private Integer itemId;

    @Column(name = "expected_quantity")
    private BigDecimal expectedQuantity;

    @Column(name = "counted_quantity")
    private BigDecimal countedQuantity;

    @Column(name = "variance")
    private BigDecimal variance;

    @Column(name = "unit_of_measure")
    private String unitOfMeasure;

    @Column(name = "notes")
    private String notes;

    @Column(name = "is_recounted")
    private Boolean isRecounted;

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
    public InventoryCountDetail() {}

    // Getters and setters
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

        InventoryCountDetail that = (InventoryCountDetail) o;

        return countDetailId != null ? countDetailId.equals(that.countDetailId) : that.countDetailId == null;
    }

    @Override
    public int hashCode() {
        return countDetailId != null ? countDetailId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "InventoryCountDetail{" +
                "countDetailId=" + countDetailId +
                ", countId=" + countId +
                ", itemId=" + itemId +
                ", expectedQuantity=" + expectedQuantity +
                ", countedQuantity=" + countedQuantity +
                ", variance=" + variance +
                ", unitOfMeasure='" + unitOfMeasure + '\'' +
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