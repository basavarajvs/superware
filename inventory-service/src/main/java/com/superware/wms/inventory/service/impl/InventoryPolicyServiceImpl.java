package com.superware.wms.inventory.service.impl;

import com.superware.wms.inventory.entity.InventoryPolicy;
import com.superware.wms.inventory.exception.ResourceNotFoundException;
import com.superware.wms.inventory.repository.InventoryPolicyRepository;
import com.superware.wms.inventory.service.InventoryPolicyService;
import com.superware.wms.tenant.context.TenantContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of the InventoryPolicyService interface.
 */
@Service
@Transactional
public class InventoryPolicyServiceImpl implements InventoryPolicyService {

    private final InventoryPolicyRepository inventoryPolicyRepository;

    @Autowired
    public InventoryPolicyServiceImpl(InventoryPolicyRepository inventoryPolicyRepository) {
        this.inventoryPolicyRepository = inventoryPolicyRepository;
    }

    @Override
    public Page<InventoryPolicy> getAllPolicies(Pageable pageable) {
        return inventoryPolicyRepository.findAll(pageable);
    }

    @Override
    public InventoryPolicy getPolicyById(Integer id) {
        return inventoryPolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryPolicy", "id", id));
    }

    @Override
    public InventoryPolicy createPolicy(InventoryPolicy policy) {
        policy.setTenantId(Integer.valueOf(TenantContextHolder.getCurrentTenant()));
        policy.setCreatedAt(LocalDateTime.now());
        policy.setUpdatedAt(LocalDateTime.now());
        policy.setCreatedBy(getCurrentUserId());
        return inventoryPolicyRepository.save(policy);
    }

    @Override
    public InventoryPolicy updatePolicy(Integer id, InventoryPolicy policyDetails) {
        InventoryPolicy policy = getPolicyById(id);
        policy.setProductId(policyDetails.getProductId());
        policy.setFacilityId(policyDetails.getFacilityId());
        policy.setMinStockLevel(policyDetails.getMinStockLevel());
        policy.setMaxStockLevel(policyDetails.getMaxStockLevel());
        policy.setReorderPoint(policyDetails.getReorderPoint());
        policy.setReorderQuantity(policyDetails.getReorderQuantity());
        policy.setAbcCategory(policyDetails.getAbcCategory());
        policy.setValuationMethod(policyDetails.getValuationMethod());
        policy.setIsActive(policyDetails.getIsActive());
        policy.setUpdatedAt(LocalDateTime.now());
        policy.setUpdatedBy(getCurrentUserId());
        return inventoryPolicyRepository.save(policy);
    }

    @Override
    public void deletePolicy(Integer id) {
        InventoryPolicy policy = getPolicyById(id);
        policy.setIsDeleted(true);
        policy.setUpdatedAt(LocalDateTime.now());
        policy.setUpdatedBy(getCurrentUserId());
        inventoryPolicyRepository.save(policy);
    }

    @Override
    public List<InventoryPolicy> getPoliciesByProductId(Integer productId) {
        return inventoryPolicyRepository.findByProductId(productId);
    }

    @Override
    public List<InventoryPolicy> getPoliciesByFacilityId(Integer facilityId) {
        return inventoryPolicyRepository.findByFacilityId(facilityId);
    }

    private Integer getCurrentUserId() {
        // In a real implementation, this would come from the security context
        return 1;
    }
}