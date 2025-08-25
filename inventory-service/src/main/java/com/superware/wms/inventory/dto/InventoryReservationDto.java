package com.superware.wms.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "Inventory reservation data transfer object")
public class InventoryReservationDto {

    @Schema(description = "Unique identifier of the inventory reservation", example = "1")
    private Integer reservationId;

    @Schema(description = "Identifier of the tenant", example = "1")
    private Integer tenantId;

    @Schema(description = "Type of reservation", example = "ORDER")
    private String reservationType;

    @NotBlank(message = "Status is required")
    @Schema(description = "Status of the reservation (e.g., RESERVED, RELEASED, CONFIRMED)", example = "RESERVED")
    private String status;

    @Schema(description = "Reference number", example = "REF123456")
    private String referenceNumber;

    @NotBlank(message = "Reference type is required")
    @Schema(description = "Reference type (e.g., ORDER, WORK_ORDER)", example = "ORDER")
    private String referenceType;

    @NotNull(message = "Reference ID is required")
    @Schema(description = "Reference ID", example = "100")
    private Integer referenceId;

    @Schema(description = "Requested date")
    private LocalDateTime requestedDate;

    @Schema(description = "Expiry date")
    private LocalDateTime expiryDate;

    @Schema(description = "Priority", example = "1")
    private Integer priority;

    @Schema(description = "Notes about the reservation")
    private String notes;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "ID of the user who created the reservation", example = "1")
    private Integer createdBy;

    @Schema(description = "ID of the user who last updated the reservation", example = "1")
    private Integer updatedBy;

    @Schema(description = "Whether the reservation is deleted", example = "false")
    private Boolean isDeleted;

    // Constructors
    public InventoryReservationDto() {
    }

    // Getters and Setters
    public Integer getReservationId() {
        return reservationId;
    }

    public void setReservationId(Integer reservationId) {
        this.reservationId = reservationId;
    }

    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public String getReservationType() {
        return reservationType;
    }

    public void setReservationType(String reservationType) {
        this.reservationType = reservationType;
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

    public LocalDateTime getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(LocalDateTime requestedDate) {
        this.requestedDate = requestedDate;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
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

        InventoryReservationDto that = (InventoryReservationDto) o;

        return reservationId != null ? reservationId.equals(that.reservationId) : that.reservationId == null;
    }

    @Override
    public int hashCode() {
        return reservationId != null ? reservationId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "InventoryReservationDto{" +
                "reservationId=" + reservationId +
                ", tenantId=" + tenantId +
                ", reservationType='" + reservationType + '\'' +
                ", status='" + status + '\'' +
                ", referenceNumber='" + referenceNumber + '\'' +
                ", referenceType='" + referenceType + '\'' +
                ", referenceId=" + referenceId +
                ", requestedDate=" + requestedDate +
                ", expiryDate=" + expiryDate +
                ", priority=" + priority +
                ", notes='" + notes + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", createdBy=" + createdBy +
                ", updatedBy=" + updatedBy +
                ", isDeleted=" + isDeleted +
                '}';
    }
}