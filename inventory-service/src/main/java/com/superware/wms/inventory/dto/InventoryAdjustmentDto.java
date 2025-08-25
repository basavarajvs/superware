package com.superware.wms.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Inventory adjustment data transfer object")
public class InventoryAdjustmentDto {

    @Schema(description = "Unique identifier of the inventory adjustment", example = "1")
    private Integer adjustmentId;

    @Schema(description = "Identifier of the tenant", example = "1")
    private Integer tenantId;

    @Schema(description = "Adjustment number", example = "ADJ2023001")
    private String adjustmentNumber;

    @Schema(description = "Date of the adjustment")
    private LocalDateTime adjustmentDate;

    @NotBlank(message = "Status is required")
    @Schema(description = "Status of the adjustment (e.g., PENDING, APPROVED, REJECTED)", example = "APPROVED")
    private String status;

    @NotBlank(message = "Adjustment type is required")
    @Schema(description = "Type of adjustment (e.g., INCREASE, DECREASE)", example = "INCREASE")
    private String adjustmentType;

    @Schema(description = "Reason code for the adjustment", example = "DAMAGE")
    private String reasonCode;

    @Schema(description = "Reference number", example = "REF123456")
    private String referenceNumber;

    @Schema(description = "Reference type", example = "ORDER")
    private String referenceType;

    @Schema(description = "Reference ID", example = "100")
    private Integer referenceId;

    @Schema(description = "Notes about the adjustment")
    private String notes;

    @Schema(description = "Whether the adjustment is approved", example = "true")
    private Boolean isApproved;

    @Schema(description = "ID of the user who approved the adjustment", example = "1")
    private Integer approvedBy;

    @Schema(description = "Approval timestamp")
    private LocalDateTime approvedAt;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "ID of the user who created the adjustment", example = "1")
    private Integer createdBy;

    @Schema(description = "ID of the user who last updated the adjustment", example = "1")
    private Integer updatedBy;

    @Schema(description = "Whether the adjustment is deleted", example = "false")
    private Boolean isDeleted;

    // Constructors
    public InventoryAdjustmentDto() {
    }

    // Getters and Setters
    public Integer getAdjustmentId() {
        return adjustmentId;
    }

    public void setAdjustmentId(Integer adjustmentId) {
        this.adjustmentId = adjustmentId;
    }

    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public String getAdjustmentNumber() {
        return adjustmentNumber;
    }

    public void setAdjustmentNumber(String adjustmentNumber) {
        this.adjustmentNumber = adjustmentNumber;
    }

    public LocalDateTime getAdjustmentDate() {
        return adjustmentDate;
    }

    public void setAdjustmentDate(LocalDateTime adjustmentDate) {
        this.adjustmentDate = adjustmentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdjustmentType() {
        return adjustmentType;
    }

    public void setAdjustmentType(String adjustmentType) {
        this.adjustmentType = adjustmentType;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public Integer getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Integer referenceId) {
        this.referenceId = referenceId;
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

        InventoryAdjustmentDto that = (InventoryAdjustmentDto) o;

        return adjustmentId != null ? adjustmentId.equals(that.adjustmentId) : that.adjustmentId == null;
    }

    @Override
    public int hashCode() {
        return adjustmentId != null ? adjustmentId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "InventoryAdjustmentDto{" +
                "adjustmentId=" + adjustmentId +
                ", tenantId=" + tenantId +
                ", adjustmentNumber='" + adjustmentNumber + '\'' +
                ", adjustmentDate=" + adjustmentDate +
                ", status='" + status + '\'' +
                ", adjustmentType='" + adjustmentType + '\'' +
                ", reasonCode='" + reasonCode + '\'' +
                ", referenceNumber='" + referenceNumber + '\'' +
                ", referenceType='" + referenceType + '\'' +
                ", referenceId=" + referenceId +
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