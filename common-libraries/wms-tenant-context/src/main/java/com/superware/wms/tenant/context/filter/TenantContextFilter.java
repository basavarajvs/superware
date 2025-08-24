package com.superware.wms.tenant.context.filter;

import com.superware.wms.tenant.context.TenantContextHolder;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

/**
 * Servlet Filter implementation that extracts the tenant ID from the X-Tenant-ID header
 * and sets it in the TenantContextHolder.
 */
public class TenantContextFilter implements Filter {
    
    private static final String TENANT_HEADER = "X-Tenant-ID";
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization logic if needed
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        try {
            // Extract tenant ID from header if request is HTTP
            if (request instanceof HttpServletRequest) {
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                String tenantId = httpRequest.getHeader(TENANT_HEADER);
                TenantContextHolder.setCurrentTenant(tenantId);
            }
            
            // Continue with the filter chain
            chain.doFilter(request, response);
        } finally {
            // Always clear the tenant context to prevent memory leaks
            TenantContextHolder.clear();
        }
    }
    
    @Override
    public void destroy() {
        // Cleanup logic if needed
    }
}