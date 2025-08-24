package com.superware.wms.inventory.service;

import com.superware.wms.tenant.context.TenantContextHolder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.stereotype.Service;

/**
 * Service class demonstrating how to enable tenant filters.
 */
@Service
public class TenantFilterService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Enable the tenant filter for the current session.
     */
    public void enableTenantFilter() {
        String tenantId = TenantContextHolder.getCurrentTenant();
        if (tenantId != null) {
            Session session = entityManager.unwrap(Session.class);
            Filter filter = session.enableFilter("tenantFilter");
            filter.setParameter("tenantId", tenantId);
        }
    }
}