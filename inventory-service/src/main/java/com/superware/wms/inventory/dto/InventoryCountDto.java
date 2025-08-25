package com.superware.wms.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "Inventory count data transfer object")
public class InventoryCountDto {

    @Schema(description = "Unique identifier of the inventory count", example = "1")
    private Integer countId;

    @Schema(description = "Identifier of the tenant", example = "1")
    private Integer tenantId;

    @Schema(description = "Count number", example = "CNT2023001")
    private String countNumber;

    @NotBlank(message = "Count type is required")
    @Schema(description = "Type of count (e.g., CYCLE, PHYSICAL)", example = "CYCLE")
    private String countType;

    @NotBlank(message = "Status is required")
    @Schema(description = "Status of the count (e.g., IN_PROGRESS, COMPLETED, CANCELLED)", example = "IN_PROGRESS")
    private String status;

    @Schema(description = "Start date of the count")
    private LocalDateTime startDate;

    @Schema(description = "End date of the count")
    private LocalDateTime endDate;

    @Schema(description = "Identifier of the facility", example = "3")
    private Integer facilityId;

    @Schema(description = "Identifier of the zone", example = "5")
    private Integer zoneId;

    @Schema(description = "Identifier of the location", example = "10")
    private Integer locationId;

    @Schema(description = "Identifier of the product", example = "100")
    private Integer productId;

    @Schema(description = "Identifier of the category", example = "20")
    private Integer categoryId;

    @Schema(description = "Notes about the count")
    private String notes;

    @Schema(description = "Whether the count is approved", example = "false")
    private Boolean isApproved;

    @Schema(description = "ID of the user who approved the count", example = "1")
    private Integer approvedBy;

    @Schema(description = "Approval timestamp")
    private LocalDateTime approvedAt;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "ID of the user who created the count", example = "1")
    private Integer createdBy;

    @Schema(description = "ID of the user who last updated the count", example = "1")
    private Integer updatedBy;

    @Schema(description = "Whether the count is deleted", example = "false")
    private Boolean isDeleted;

    // Constructors
    public InventoryCountDto() {
    }

    // Getters and Setters
    public Integer getCountId() {
        return countId;
    }

    public void setCountId(Integer countId) {
        this.countId = countId;
    }

    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public String getCountNumber() {
        return countNumber;
    }

    public void setCountNumber(String countNumber) {
        this.countNumber = countNumber;
    }

    public String getCountType() {
        return countType;
    }

    public void setCountType(String countType) {
        this.countType = countType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Integer getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(Integer facilityId) {
        this.facilityId = facilityId;
    }

    public Integer getZoneId() {
        return zoneId;
    }

    public void setZoneId(Integer zoneId) {
        this.zoneId = zoneId;
    }

    public Integer getLocationId() {
        return locationId;
    }

    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getIsApproved() {
        return isApproved;
    }

    public void setIsApproved(Boolean isApproved) {
        this.isApproved = isApproved;
    }

    public Integer getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Integer approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
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

        InventoryCountDto that = (InventoryCountDto) o;

        return countId != null ? countId.equals(that.countId) : that.countId == null;
    }

    @Override
    public int hashCode() {
        return countId != null ? countId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "InventoryCountDto{" +
                "countId=" + countId +
                ", tenantId=" + tenantId +
                ", countNumber='" + countNumber + '\'' +
                ", countType='" + countType + '\'' +
                ", status='" + status + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", facilityId=" + facilityId +
                ", zoneId=" + zoneId +
                ", locationId=" + locationId +
                ", productId=" + productId +
                ", categoryId=" + categoryId +
                ", notes='" + notes + '\'' +
                ", isApproved=" + isApproved +
                ", approvedBy=" + approvedBy +
                ", approvedAt=" + approvedAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", createdBy=" + createdBy +
                ", updatedBy=" + updatedBy +
                ", isDeleted=" + isDeleted +
                '}';
    }
}