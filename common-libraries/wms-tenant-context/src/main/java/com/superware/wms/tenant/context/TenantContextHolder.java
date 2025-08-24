package com.superware.wms.tenant.context;

/**
 * Tenant context holder using ThreadLocal to store tenant information.
 */
public class TenantContextHolder {
    
    private static final ThreadLocal<String> tenantContext = new ThreadLocal<>();
    
    /**
     * Sets the current tenant ID in the context.
     * 
     * @param tenantId the tenant ID to set
     */
    public static void setCurrentTenant(String tenantId) {
        tenantContext.set(tenantId);
    }
    
    /**
     * Gets the current tenant ID from the context.
     * 
     * @return the current tenant ID, or null if not set
     */
    public static String getCurrentTenant() {
        return tenantContext.get();
    }
    
    /**
     * Clears the tenant context.
     */
    public static void clear() {
        tenantContext.remove();
    }
}