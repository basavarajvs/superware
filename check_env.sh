#!/bin/bash

# Script to check if required tools are installed

echo "Checking for required tools..."

# Check Java
echo "Checking Java..."
if command -v java &> /dev/null
then
    echo "✅ Java is installed"
    java --version
    echo ""
else
    echo "❌ Java is not installed. Please install Java to continue."
    echo ""
fi

# Check Maven
echo "Checking Maven..."
if command -v mvn &> /dev/null
then
    echo "✅ Maven is installed"
    mvn --version
    echo ""
else
    echo "❌ Maven is not installed. Please install Maven to continue."
    echo ""
fi

# Check Docker
echo "Checking Docker..."
if command -v docker &> /dev/null
then
    echo "✅ Docker is installed"
    docker --version
    echo ""
else
    echo "❌ Docker is not installed. Please install Docker to continue."
    echo ""
fi

echo "Check completed."