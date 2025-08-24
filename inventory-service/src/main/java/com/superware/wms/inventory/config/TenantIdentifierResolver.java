package com.superware.wms.inventory.config;

import com.superware.wms.tenant.context.TenantContextHolder;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

/**
 * Hibernate tenant identifier resolver that retrieves the current tenant ID
 * from the TenantContextHolder.
 */
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContextHolder.getCurrentTenant();
        return tenantId != null ? tenantId : "DEFAULT";
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }
}