# Instrucciones para Cargar las 26 Vacunas a Firebase Firestore

## Resumen

Este documento explica cómo cargar las 26 vacunas predefinidas del sistema a Firebase Firestore.

## Vacunas Incluidas

El script carga **26 vacunas** organizadas por especie:

- **Perros (DOG)**: 8 vacunas
  - Rabia (Perros) [CORE]
  - Moquillo Canino (Distemper) [CORE]
  - Parvovirus Canino [CORE]
  - Hepatitis Infecciosa Canina (Adenovirus tipo 2) [CORE]
  - Leptospirosis [OPCIONAL]
  - Bordetella (Tos de las Perreras) [OPCIONAL]
  - Parainfluenza Canina [OPCIONAL]
  - Coronavirus Canino [OPCIONAL]

- **Gatos (CAT)**: 5 vacunas
  - Rabia (Gatos) [CORE]
  - FVRCP (Trivalente Felina) [CORE]
  - Leucemia Felina (FeLV) [OPCIONAL]
  - Clamidia Felina [OPCIONAL]
  - Inmunodeficiencia Felina (FIV) [OPCIONAL]

- **Conejos (RABBIT)**: 3 vacunas
  - Mixomatosis [CORE]
  - Enfermedad Hemorrágica Vírica del Conejo (RHD/VHD) [CORE]
  - RHD2 (Variante 2 de Enfermedad Hemorrágica) [CORE]

- **Hámsters (HAMSTER)**: 1 vacuna
  - Rabia (Roedores Exóticos) [OPCIONAL]

- **Tortugas (TURTLE)**: 1 vacuna
  - Vacuna Experimental para Reptiles [OPCIONAL]

- **Periquitos (PARAKEET)**: 3 vacunas
  - Poliomavirus Aviar [OPCIONAL]
  - Enfermedad de Newcastle [OPCIONAL]
  - Viruela Aviar (Poxvirus) [OPCIONAL]

- **Patos (DUCK)**: 5 vacunas
  - Cólera Aviar (Pasteurella) [CORE]
  - Enfermedad de Newcastle (Patos) [CORE]
  - Hepatitis Viral del Pato (DVH) [CORE]
  - Influenza Aviar [OPCIONAL]
  - Enteritis Viral del Pato [OPCIONAL]

## Prerequisitos

1. **Credenciales de Firebase**: Necesitas el archivo de Service Account Key de Firebase

## Paso 1: Obtener Credenciales de Firebase

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto AnyPet
3. Click en el ícono de engranaje ⚙️ → **Project Settings**
4. Ve a la pestaña **Service Accounts**
5. Click en **Generate New Private Key**
6. Se descargará un archivo JSON

## Paso 2: Configurar las Credenciales

Guarda el archivo descargado como `.secrets/firebase-authkey.json`:

```bash
# El directorio .secrets ya existe, solo copia el archivo
cp ~/Downloads/tu-archivo-descargado.json .secrets/firebase-authkey.json
```

## Paso 3: Ejecutar el Script

### Opción 1: Script Bash (Recomendado)

```bash
./load-vaccines-to-firestore.sh
```

### Opción 2: Maven Directamente

```bash
# Compilar primero
./mvnw clean compile -DskipTests

# Ejecutar el script
./mvnw exec:java \
    -Dexec.mainClass="com.bydaffi.anypetbackend.scripts.LoadVaccinesToFirestore" \
    -Dexec.cleanupDaemonThreads=false
```

### Opción 3: Compilar y Ejecutar Manualmente

```bash
# Compilar con dependencias
./mvnw clean package -DskipTests

# Ejecutar directamente con Java
java -cp "target/classes:target/dependency/*" \
    com.bydaffi.anypetbackend.scripts.LoadVaccinesToFirestore
```

## Estructura de Datos en Firestore

Cada vacuna se guarda en la colección `vaccines` con la siguiente estructura:

```json
{
  "name": "Rabia (Perros)",
  "targetSpecies": "DOG",
  "description": "Vacuna contra el virus de la rabia...",
  "isCore": true
}
```

El ID del documento es el nombre sanitizado (sin espacios, paréntesis, etc.):
- "Rabia (Perros)" → `rabia_perros`
- "FVRCP (Trivalente Felina)" → `fvrcp_trivalente_felina`

## Verificación

Después de ejecutar el script, deberías ver:

```
======================================================================
  SCRIPT DE CARGA DE VACUNAS A FIREBASE FIRESTORE
======================================================================

→ Inicializando conexión con Firebase...
✓ Conexión establecida con Firestore

──────────────────────────────────────────────────────────────────────
  VACUNAS PARA PERROS (DOG) - 8 vacunas
──────────────────────────────────────────────────────────────────────
   1. ✓ Rabia (Perros)                                        [CORE]
   2. ✓ Moquillo Canino (Distemper)                          [CORE]
   ...

======================================================================
  ✓ COMPLETADO: 26 vacunas cargadas exitosamente
======================================================================
```

## Verificar en Firebase Console

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto
3. Ve a **Firestore Database**
4. Deberías ver la colección `vaccines` con 26 documentos

## Archivos Creados

- `src/main/java/com/bydaffi/anypetbackend/scripts/LoadVaccinesToFirestore.java` - Script principal
- `src/main/java/com/bydaffi/anypetbackend/config/FirebaseVaccineLoader.java` - Loader automático (deshabilitado)
- `load-vaccines-to-firestore.sh` - Script bash para ejecución fácil
- `.secrets/README.md` - Instrucciones sobre credenciales

## Solución de Problemas

### Error: "No such file: .secrets/firebase-authkey.json"

Asegúrate de haber copiado el archivo de credenciales al directorio correcto.

### Error de autenticación de Firebase

Verifica que:
- El archivo JSON sea válido
- Tenga los permisos correctos para tu proyecto
- El proyecto de Firebase esté activo

### Error al compilar

Ejecuta primero:
```bash
./mvnw clean install
```

## Notas

- El script es **idempotente**: Si ejecutas el script múltiples veces, sobrescribirá los documentos existentes (no duplicará)
- Las vacunas usan IDs determinísticos basados en su nombre
- El flag `isCore` indica si la vacuna es obligatoria (core) u opcional
