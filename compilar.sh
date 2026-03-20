#!/usr/bin/env bash
# ============================================================
# compilar.sh — Script de compilación y ejecución
# Generador de Recibos de Pago — CARDAMOMO Y CELERY, C.A.
# ============================================================

set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "============================================"
echo "  GENERADOR DE RECIBOS DE PAGO"
echo "  Compilando con Maven..."
echo "============================================"

# Compilar y empaquetar
mvn clean package -q

echo ""
echo "✅ Compilación exitosa."
echo "   Ejecutable: recibos-app.jar"
echo ""
echo "============================================"
echo "  Iniciando aplicación..."
echo "============================================"
echo ""

# Ejecutar el JAR generado
java -jar recibos-app.jar
