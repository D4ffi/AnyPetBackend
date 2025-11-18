#!/bin/bash

echo "=========================================="
echo "  CARGA DE VACUNAS A FIREBASE FIRESTORE"
echo "=========================================="
echo ""

# Verificar que existan las credenciales
if [ ! -f ".secrets/firebase-authkey.json" ]; then
    echo "❌ ERROR: No se encontró el archivo de credenciales Firebase"
    echo ""
    echo "Por favor, coloca tu archivo de credenciales en:"
    echo "  .secrets/firebase-authkey.json"
    echo ""
    echo "Consulta .secrets/README.md para más información."
    exit 1
fi

echo "✓ Credenciales de Firebase encontradas"
echo ""

# Compilar el proyecto primero
echo "→ Compilando el proyecto..."
./mvnw clean compile -DskipTests -q

if [ $? -ne 0 ]; then
    echo "❌ Error al compilar el proyecto"
    exit 1
fi

echo "✓ Proyecto compilado exitosamente"
echo ""

# Ejecutar el script
echo "→ Ejecutando script de carga de vacunas..."
echo ""

./mvnw exec:java \
    -Dexec.mainClass="com.bydaffi.anypetbackend.scripts.LoadVaccinesToFirestore" \
    -Dexec.cleanupDaemonThreads=false \
    -q

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ ¡Script ejecutado exitosamente!"
else
    echo ""
    echo "❌ Ocurrió un error durante la ejecución"
    exit 1
fi
