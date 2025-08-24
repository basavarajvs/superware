package com.superware.wms.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * Marker interface for inventory repositories.
 */
@NoRepositoryBean
public interface InventoryRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {
}