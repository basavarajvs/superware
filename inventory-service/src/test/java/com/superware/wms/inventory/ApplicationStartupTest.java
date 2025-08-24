package com.superware.wms.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class ApplicationStartupTest {

    @Test
    public void contextLoads() {
        // This test verifies that the Spring context loads successfully
        // If the application starts, it means our multi-tenancy configuration is correct
    }
}