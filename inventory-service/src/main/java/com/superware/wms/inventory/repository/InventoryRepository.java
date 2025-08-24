package com.superware.wms.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * Base repository interface for inventory repositories.
 * Individual repositories should extend TenantAwareRepository directly for multi-tenancy.
 */
@NoRepositoryBean
public interface InventoryRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {
}