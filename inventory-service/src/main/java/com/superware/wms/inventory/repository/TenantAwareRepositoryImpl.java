package com.superware.wms.inventory.repository;

import com.superware.wms.inventory.service.TenantEntityService;
import com.superware.wms.tenant.context.TenantContextHolder;
import jakarta.persistence.EntityManager;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of TenantAwareRepository that provides tenant filtering functionality.
 */
public class TenantAwareRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements TenantAwareRepository<T, ID> {

    private final EntityManager entityManager;
    
    @Autowired
    private TenantEntityService tenantEntityService;

    public TenantAwareRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
    }

    /**
     * Enable tenant filter before executing any repository operation.
     */
    private void enableTenantFilter() {
        String tenantId = TenantContextHolder.getCurrentTenant();
        if (tenantId != null) {
            try {
                Session session = entityManager.unwrap(Session.class);
                Filter filter = session.enableFilter("tenantFilter");
                filter.setParameter("tenantId", tenantId);
            } catch (Exception e) {
                // Log the error but don't fail the operation
                // This allows the application to work even if tenant filtering fails
                System.err.println("Failed to enable tenant filter: " + e.getMessage());
            }
        }
    }

    @Override
    public void delete(T entity) {
        enableTenantFilter();
        super.delete(entity);
    }

    @Override
    public void deleteById(ID id) {
        enableTenantFilter();
        super.deleteById(id);
    }

    @Override
    public void deleteAll() {
        enableTenantFilter();
        super.deleteAll();
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        enableTenantFilter();
        super.deleteAll(entities);
    }

    @Override
    public void deleteAllById(Iterable<? extends ID> ids) {
        enableTenantFilter();
        super.deleteAllById(ids);
    }

    @Override
    public <S extends T> S save(S entity) {
        // Set tenant ID before saving
        if (tenantEntityService != null) {
            tenantEntityService.setTenantIdIfPossible(entity);
        }
        enableTenantFilter();
        return super.save(entity);
    }

    @Override
    public <S extends T> List<S> saveAll(Iterable<S> entities) {
        // Set tenant ID before saving
        if (tenantEntityService != null) {
            tenantEntityService.setTenantIdIfPossible(entities);
        }
        enableTenantFilter();
        return super.saveAll(entities);
    }

    @Override
    public Optional<T> findById(ID id) {
        enableTenantFilter();
        return super.findById(id);
    }

    @Override
    public boolean existsById(ID id) {
        enableTenantFilter();
        return super.existsById(id);
    }

    @Override
    public List<T> findAll() {
        enableTenantFilter();
        return super.findAll();
    }

    @Override
    public List<T> findAllById(Iterable<ID> ids) {
        enableTenantFilter();
        return super.findAllById(ids);
    }

    @Override
    public long count() {
        enableTenantFilter();
        return super.count();
    }
}
