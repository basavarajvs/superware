package com.superware.wms.inventory.service;

import com.superware.wms.inventory.entity.InventoryPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for managing inventory policies.
 */
public interface InventoryPolicyService {
    
    /**
     * Get all inventory policies with pagination.
     *
     * @param pageable pagination information
     * @return page of inventory policies
     */
    Page<InventoryPolicy> getAllPolicies(Pageable pageable);
    
    /**
     * Get an inventory policy by ID.
     *
     * @param id the ID of the inventory policy
     * @return the inventory policy
     * @throws com.superware.wms.inventory.exception.ResourceNotFoundException if the inventory policy is not found
     */
    InventoryPolicy getPolicyById(Integer id);
    
    /**
     * Create a new inventory policy.
     *
     * @param policy the inventory policy to create
     * @return the created inventory policy
     */
    InventoryPolicy createPolicy(InventoryPolicy policy);
    
    /**
     * Update an existing inventory policy.
     *
     * @param id the ID of the inventory policy to update
     * @param policyDetails the updated inventory policy data
     * @return the updated inventory policy
     * @throws com.superware.wms.inventory.exception.ResourceNotFoundException if the inventory policy is not found
     */
    InventoryPolicy updatePolicy(Integer id, InventoryPolicy policyDetails);
    
    /**
     * Delete an inventory policy.
     *
     * @param id the ID of the inventory policy to delete
     * @throws com.superware.wms.inventory.exception.ResourceNotFoundException if the inventory policy is not found
     */
    void deletePolicy(Integer id);
    
    /**
     * Get inventory policies by product ID.
     *
     * @param productId the ID of the product
     * @return list of inventory policies for the product
     */
    List<InventoryPolicy> getPoliciesByProductId(Integer productId);
    
    /**
     * Get inventory policies by facility ID.
     *
     * @param facilityId the ID of the facility
     * @return list of inventory policies for the facility
     */
    List<InventoryPolicy> getPoliciesByFacilityId(Integer facilityId);
}