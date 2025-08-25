package com.superware.wms.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "Inventory transaction data transfer object")
public class InventoryTransactionDto {

    @Schema(description = "Unique identifier of the inventory transaction", example = "1")
    private Integer transactionId;

    @Schema(description = "Identifier of the tenant", example = "1")
    private Integer tenantId;

    @NotBlank(message = "Transaction type is required")
    @Schema(description = "Type of transaction (e.g., RECEIPT, ISSUE, TRANSFER)", example = "RECEIPT")
    private String transactionType;

    @Schema(description = "Date of the transaction")
    private LocalDateTime transactionDate;

    @NotBlank(message = "Status is required")
    @Schema(description = "Status of the transaction (e.g., PENDING, COMPLETED, CANCELLED)", example = "COMPLETED")
    private String status;

    @Schema(description = "Reference number", example = "REF123456")
    private String referenceNumber;

    @Schema(description = "Reference type", example = "ORDER")
    private String referenceType;

    @Schema(description = "Reference ID", example = "100")
    private Integer referenceId;

    @Schema(description = "Source type", example = "SUPPLIER")
    private String sourceType;

    @Schema(description = "Source ID", example = "50")
    private Integer sourceId;

    @Schema(description = "Destination type", example = "WAREHOUSE")
    private String destinationType;

    @Schema(description = "Destination ID", example = "3")
    private Integer destinationId;

    @Schema(description = "Notes about the transaction")
    private String notes;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "ID of the user who created the transaction", example = "1")
    private Integer createdBy;

    @Schema(description = "ID of the user who last updated the transaction", example = "1")
    private Integer updatedBy;

    @Schema(description = "Whether the transaction is deleted", example = "false")
    private Boolean isDeleted;

    // Constructors
    public InventoryTransactionDto() {
    }

    // Getters and Setters
    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Integer getSourceId() {
        return sourceId;
    }

    public void setSourceId(Integer sourceId) {
        this.sourceId = sourceId;
    }

    public String getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(String destinationType) {
        this.destinationType = destinationType;
    }

    public Integer getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(Integer destinationId) {
        this.destinationId = destinationId;
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

        InventoryTransactionDto that = (InventoryTransactionDto) o;

        return transactionId != null ? transactionId.equals(that.transactionId) : that.transactionId == null;
    }

    @Override
    public int hashCode() {
        return transactionId != null ? transactionId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "InventoryTransactionDto{" +
                "transactionId=" + transactionId +
                ", tenantId=" + tenantId +
                ", transactionType='" + transactionType + '\'' +
                ", transactionDate=" + transactionDate +
                ", status='" + status + '\'' +
                ", referenceNumber='" + referenceNumber + '\'' +
                ", referenceType='" + referenceType + '\'' +
                ", referenceId=" + referenceId +
                ", sourceType='" + sourceType + '\'' +
                ", sourceId=" + sourceId +
                ", destinationType='" + destinationType + '\'' +
                ", destinationId=" + destinationId +
                ", notes='" + notes + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", createdBy=" + createdBy +
                ", updatedBy=" + updatedBy +
                ", isDeleted=" + isDeleted +
                '}';
    }
}