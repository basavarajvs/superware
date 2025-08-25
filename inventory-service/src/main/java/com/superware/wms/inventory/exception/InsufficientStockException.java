package com.superware.wms.inventory.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InsufficientStockException extends RuntimeException {
    
    private final String resourceName;
    private final Integer resourceId;
    private final String requestedAction;
    private final String availableQuantity;
    private final String requestedQuantity;

    public InsufficientStockException(String resourceName, Integer resourceId, String requestedAction, 
                                    String availableQuantity, String requestedQuantity) {
        super(String.format("Insufficient stock for %s (ID: %d) to %s. Available: %s, Requested: %s", 
                          resourceName, resourceId, requestedAction, availableQuantity, requestedQuantity));
        this.resourceName = resourceName;
        this.resourceId = resourceId;
        this.requestedAction = requestedAction;
        this.availableQuantity = availableQuantity;
        this.requestedQuantity = requestedQuantity;
    }

    public String getResourceName() {
        return resourceName;
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public String getRequestedAction() {
        return requestedAction;
    }

    public String getAvailableQuantity() {
        return availableQuantity;
    }

    public String getRequestedQuantity() {
        return requestedQuantity;
    }
}