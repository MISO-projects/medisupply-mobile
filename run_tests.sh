#!/bin/bash

# Script para ejecutar tests del módulo de Inventario
# Uso: ./run_tests.sh [opcion]

echo "🧪 Script de Tests - Módulo Inventario MediSupply"
echo "=================================================="
echo ""

case "$1" in
  "unit")
    echo "📋 Ejecutando Tests Unitarios..."
    ./gradlew testDebugUnitTest
    ;;
  "ui")
    echo "🎯 Ejecutando Tests de UI (requiere dispositivo/emulador)..."
    ./gradlew connectedDebugAndroidTest
    ;;
  "viewmodel")
    echo "🧠 Ejecutando Tests del ViewModel..."
    ./gradlew test --tests "com.medisupply.viewmodels.InventarioViewModelTest"
    ;;
  "repository")
    echo "💾 Ejecutando Tests del Repository..."
    ./gradlew test --tests "com.medisupply.repositories.InventarioRepositoryTest"
    ;;
  "adapter")
    echo "📊 Ejecutando Tests del Adapter..."
    ./gradlew test --tests "com.medisupply.adapters.ProductosAdapterTest"
    ;;
  "fragment")
    echo "🖼️ Ejecutando Tests del Fragment (requiere dispositivo/emulador)..."
    ./gradlew connectedAndroidTest --tests "com.medisupply.ui.InventarioFragmentTest"
    ;;
  "coverage")
    echo "📈 Generando Reporte de Cobertura..."
    ./gradlew testDebugUnitTestCoverage
    echo ""
    echo "✅ Reporte generado en: app/build/reports/coverage/test/debug/index.html"
    ;;
  "all")
    echo "🚀 Ejecutando TODOS los tests..."
    echo ""
    echo "1️⃣ Tests Unitarios..."
    ./gradlew testDebugUnitTest
    echo ""
    echo "2️⃣ Tests de UI..."
    ./gradlew connectedDebugAndroidTest
    ;;
  "clean")
    echo "🧹 Limpiando proyecto..."
    ./gradlew clean
    echo "✅ Proyecto limpiado"
    ;;
  *)
    echo "Opciones disponibles:"
    echo ""
    echo "  unit       - Ejecutar todos los tests unitarios"
    echo "  ui         - Ejecutar todos los tests de UI"
    echo "  viewmodel  - Ejecutar tests del ViewModel"
    echo "  repository - Ejecutar tests del Repository"
    echo "  adapter    - Ejecutar tests del Adapter"
    echo "  fragment   - Ejecutar tests del Fragment"
    echo "  coverage   - Generar reporte de cobertura"
    echo "  all        - Ejecutar TODOS los tests"
    echo "  clean      - Limpiar proyecto"
    echo ""
    echo "Ejemplo: ./run_tests.sh unit"
    ;;
esac

echo ""
echo "=================================================="


