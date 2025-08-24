package com.superware.wms.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class ApplicationContextLoadTest {

    @Test
    public void testApplicationContextLoads() {
        // This test verifies that the Spring context loads successfully
        // If the application starts, it means our multi-tenancy configuration is correct
    }
}