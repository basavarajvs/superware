package com.superware.wms.tenant.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TenantContextHolderTest {

    @AfterEach
    public void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    public void testSetAndGetCurrentTenant() {
        // Test setting tenant ID
        TenantContextHolder.setCurrentTenant("123");
        assertThat(TenantContextHolder.getCurrentTenant()).isEqualTo("123");

        // Test changing tenant ID
        TenantContextHolder.setCurrentTenant("456");
        assertThat(TenantContextHolder.getCurrentTenant()).isEqualTo("456");
    }

    @Test
    public void testClearTenant() {
        // Set a tenant ID
        TenantContextHolder.setCurrentTenant("123");
        assertThat(TenantContextHolder.getCurrentTenant()).isEqualTo("123");

        // Clear the tenant context
        TenantContextHolder.clear();
        assertThat(TenantContextHolder.getCurrentTenant()).isNull();
    }

    @Test
    public void testThreadSafety() throws InterruptedException {
        // Test that each thread has its own tenant context
        TenantContextHolder.setCurrentTenant("main-thread");

        Thread thread = new Thread(() -> {
            assertThat(TenantContextHolder.getCurrentTenant()).isNull();
            TenantContextHolder.setCurrentTenant("child-thread");
            assertThat(TenantContextHolder.getCurrentTenant()).isEqualTo("child-thread");
        });

        thread.start();
        thread.join();

        // Verify the main thread's context is unchanged
        assertThat(TenantContextHolder.getCurrentTenant()).isEqualTo("main-thread");
    }
}