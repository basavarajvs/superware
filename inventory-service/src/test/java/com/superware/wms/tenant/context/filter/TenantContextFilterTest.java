package com.superware.wms.tenant.context.filter;

import com.superware.wms.tenant.context.TenantContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TenantContextFilterTest {

    @AfterEach
    public void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    public void testDoFilterWithTenantHeader() throws Exception {
        // Mock the request, response, and filter chain
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain filterChain = Mockito.mock(FilterChain.class);

        // Set up the mock to return a tenant ID header
        when(request.getHeader("X-Tenant-ID")).thenReturn("123");

        // Create the filter and execute it
        TenantContextFilter filter = new TenantContextFilter();
        
        // Capture the tenant context before the filter chain is called
        doAnswer(invocation -> {
            // Verify that the tenant context was set correctly before continuing the chain
            assertThat(TenantContextHolder.getCurrentTenant()).isEqualTo("123");
            return null;
        }).when(filterChain).doFilter(request, response);

        filter.doFilter(request, response, filterChain);

        // Verify that the filter chain was called
        verify(filterChain).doFilter(request, response);

        // Verify that the tenant context was cleared after the request
        assertThat(TenantContextHolder.getCurrentTenant()).isNull();
    }

    @Test
    public void testDoFilterWithoutTenantHeader() throws Exception {
        // Mock the request, response, and filter chain
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain filterChain = Mockito.mock(FilterChain.class);

        // Set up the mock to return no tenant ID header
        when(request.getHeader("X-Tenant-ID")).thenReturn(null);

        // Create the filter and execute it
        TenantContextFilter filter = new TenantContextFilter();
        filter.doFilter(request, response, filterChain);

        // Verify that the tenant context was not set
        assertThat(TenantContextHolder.getCurrentTenant()).isNull();

        // Verify that the filter chain was called
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void testDoFilterWithEmptyTenantHeader() throws Exception {
        // Mock the request, response, and filter chain
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain filterChain = Mockito.mock(FilterChain.class);

        // Set up the mock to return an empty tenant ID header
        when(request.getHeader("X-Tenant-ID")).thenReturn("");

        // Create the filter and execute it
        TenantContextFilter filter = new TenantContextFilter();
        filter.doFilter(request, response, filterChain);

        // Verify that the tenant context was not set
        assertThat(TenantContextHolder.getCurrentTenant()).isNull();

        // Verify that the filter chain was called
        verify(filterChain).doFilter(request, response);
    }
}