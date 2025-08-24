package com.superware.wms.security.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration class for WMS Security.
 * Imports the main security configuration.
 */
@AutoConfiguration
@Import(WmsSecurityConfig.class)
public class WmsSecurityAutoConfig {
}