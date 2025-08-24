#!/bin/bash

# Script to run the manual tenant filtering test

echo "Running manual tenant filtering test..."

# Compile the project
mvn compile

# Run the manual test
mvn spring-boot:run -Dspring-boot.run.main-class=com.superware.wms.inventory.ManualTenantTest

echo "Manual tenant filtering test completed!"