# Firestore Indexes

Este documento explica cómo gestionar los índices de Firestore para este proyecto.

## Problema

La consulta en `ReminderService.processDueReminders()` requiere un índice compuesto porque filtra por múltiples campos:
- `active = true` (filtro de igualdad)
- `nextExecution <= now` (filtro de rango)

## Soluciones

### Opción 1: Crear índice manualmente (Recomendado para desarrollo rápido)

1. Cuando veas el error, copia la URL que aparece en el log
2. Visita la URL en tu navegador
3. Haz clic en "Create Index" en la consola de Firebase
4. Espera 2-5 minutos mientras el índice se construye
5. El error desaparecerá una vez completado

### Opción 2: Desplegar índices usando Firebase CLI (Recomendado para producción)

1. Instala Firebase CLI si no la tienes:
   ```bash
   npm install -g firebase-tools
   ```

2. Inicia sesión en Firebase:
   ```bash
   firebase login
   ```

3. Inicializa Firebase en el proyecto (si no está inicializado):
   ```bash
   firebase init firestore
   ```
   - Selecciona tu proyecto: `animalotchi`
   - Acepta los archivos predeterminados

4. Despliega los índices definidos en `firestore.indexes.json`:
   ```bash
   firebase deploy --only firestore:indexes
   ```

## Índices actuales

El archivo `firestore.indexes.json` define los siguientes índices:

### Índice: reminders (active + nextExecution)
- **Colección**: `reminders`
- **Campos**:
  - `active` (ASCENDING)
  - `nextExecution` (ASCENDING)
- **Propósito**: Permite consultar recordatorios activos que deben ejecutarse (ReminderService.processDueReminders)

## Agregar nuevos índices

Si necesitas agregar más índices en el futuro:

1. Ejecuta la consulta que falla
2. Copia la URL del error
3. Visita la URL para crear el índice manualmente
4. Agrega la definición del índice a `firestore.indexes.json` para documentarlo
5. Despliega: `firebase deploy --only firestore:indexes`

## Verificar índices

Puedes verificar los índices existentes en:
https://console.firebase.google.com/project/animalotchi/firestore/indexes

## Notas

- Los índices pueden tomar varios minutos en construirse
- Los índices compuestos son necesarios cuando combinas filtros de igualdad con filtros de rango
- El índice `__name__` se agrega automáticamente por Firestore para ordenación determinística
