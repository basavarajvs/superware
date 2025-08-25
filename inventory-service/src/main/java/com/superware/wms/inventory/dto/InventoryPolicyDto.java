package com.superware.wms.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Inventory policy data transfer object")
public class InventoryPolicyDto {

    @Schema(description = "Unique identifier of the inventory policy", example = "1")
    private Integer policyId;

    @Schema(description = "Identifier of the tenant", example = "1")
    private Integer tenantId;

    @NotNull(message = "Product ID is required")
    @Schema(description = "Identifier of the product", example = "100")
    private Integer productId;

    @Schema(description = "Identifier of the product variant", example = "10")
    private Integer variantId;

    @Schema(description = "Identifier of the facility", example = "3")
    private Integer facilityId;

    @Schema(description = "Minimum stock level", example = "50.00")
    private BigDecimal minStockLevel;

    @Schema(description = "Maximum stock level", example = "500.00")
    private BigDecimal maxStockLevel;

    @Schema(description = "Reorder point", example = "100.00")
    private BigDecimal reorderPoint;

    @Schema(description = "Reorder quantity", example = "200.00")
    private BigDecimal reorderQuantity;

    @Schema(description = "Valuation method", example = "FIFO")
    private String valuationMethod;

    @Schema(description = "ABC class", example = "A")
    private String abcClass;

    @Schema(description = "Whether the policy is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "ID of the user who created the policy", example = "1")
    private Integer createdBy;

    @Schema(description = "ID of the user who last updated the policy", example = "1")
    private Integer updatedBy;

    @Schema(description = "Whether the policy is deleted", example = "false")
    private Boolean isDeleted;

    // Constructors
    public InventoryPolicyDto() {
    }

    // Getters and Setters
    public Integer getPolicyId() {
        return policyId;
    }

    public void setPolicyId(Integer policyId) {
        this.policyId = policyId;
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

    public Integer getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(Integer facilityId) {
        this.facilityId = facilityId;
    }

    public BigDecimal getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(BigDecimal minStockLevel) {
        this.minStockLevel = minStockLevel;
    }

    public BigDecimal getMaxStockLevel() {
        return maxStockLevel;
    }

    public void setMaxStockLevel(BigDecimal maxStockLevel) {
        this.maxStockLevel = maxStockLevel;
    }

    public BigDecimal getReorderPoint() {
        return reorderPoint;
    }

    public void setReorderPoint(BigDecimal reorderPoint) {
        this.reorderPoint = reorderPoint;
    }

    public BigDecimal getReorderQuantity() {
        return reorderQuantity;
    }

    public void setReorderQuantity(BigDecimal reorderQuantity) {
        this.reorderQuantity = reorderQuantity;
    }

    public String getValuationMethod() {
        return valuationMethod;
    }

    public void setValuationMethod(String valuationMethod) {
        this.valuationMethod = valuationMethod;
    }

    public String getAbcClass() {
        return abcClass;
    }

    public void setAbcClass(String abcClass) {
        this.abcClass = abcClass;
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

        InventoryPolicyDto that = (InventoryPolicyDto) o;

        return policyId != null ? policyId.equals(that.policyId) : that.policyId == null;
    }

    @Override
    public int hashCode() {
        return policyId != null ? policyId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "InventoryPolicyDto{" +
                "policyId=" + policyId +
                ", tenantId=" + tenantId +
                ", productId=" + productId +
                ", variantId=" + variantId +
                ", facilityId=" + facilityId +
                ", minStockLevel=" + minStockLevel +
                ", maxStockLevel=" + maxStockLevel +
                ", reorderPoint=" + reorderPoint +
                ", reorderQuantity=" + reorderQuantity +
                ", valuationMethod='" + valuationMethod + '\'' +
                ", abcClass='" + abcClass + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", createdBy=" + createdBy +
                ", updatedBy=" + updatedBy +
                ", isDeleted=" + isDeleted +
                '}';
    }
}