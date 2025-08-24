package com.superware.wms.inventory.service;

import com.superware.wms.tenant.context.TenantContextHolder;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

/**
 * Service that automatically sets tenant IDs on entities before they're saved.
 */
@Service
public class TenantEntityService {

    /**
     * Set the tenant ID on an entity if it has a setTenantId method.
     */
    public <T> T setTenantIdIfPossible(T entity) {
        if (entity == null) {
            return null;
        }

        String currentTenant = TenantContextHolder.getCurrentTenant();
        if (currentTenant == null) {
            return entity;
        }

        try {
            // Try to find and call setTenantId method
            Method setTenantIdMethod = entity.getClass().getMethod("setTenantId", Integer.class);
            if (setTenantIdMethod != null) {
                setTenantIdMethod.invoke(entity, Integer.valueOf(currentTenant));
            }
        } catch (Exception e) {
            // Entity doesn't have setTenantId method or it failed
            // This is fine - not all entities need tenant IDs
        }

        return entity;
    }

    /**
     * Set tenant ID on multiple entities.
     */
    public <T> Iterable<T> setTenantIdIfPossible(Iterable<T> entities) {
        if (entities == null) {
            return null;
        }

        for (T entity : entities) {
            setTenantIdIfPossible(entity);
        }

        return entities;
    }
}
