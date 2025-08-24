package com.superware.wms.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * Base repository interface that provides tenant-aware functionality.
 * All tenant-aware repositories should extend this interface.
 */
@NoRepositoryBean
public interface TenantAwareRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {
}