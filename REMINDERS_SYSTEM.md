# Sistema de Recordatorios con Firebase y Notificaciones Push

Este documento describe el sistema de recordatorios implementado en AnyPetBackend, que permite crear recordatorios programados que se almacenan en Firebase Firestore y envían notificaciones push a través de Firebase Cloud Messaging (FCM).

## Arquitectura del Sistema

### Componentes Principales

1. **Entidad Reminder** (`models/Reminder.java`)
   - Almacena información del recordatorio en la base de datos local (H2/PostgreSQL)
   - Campos principales:
     - `title`: Título del recordatorio
     - `scheduledTime`: Hora del día en que debe ejecutarse (formato LocalTime)
     - `repeatInterval`: Intervalo de repetición (ONCE, DAILY, WEEKLY, MONTHLY, etc.)
     - `userId`: ID del usuario (Firebase UID)
     - `deviceToken`: Token FCM del dispositivo para enviar notificaciones
     - `active`: Estado activo/inactivo
     - `nextExecution`: Próxima fecha/hora de ejecución calculada automáticamente

2. **ReminderRepository** (`repository/ReminderRepository.java`)
   - Repositorio JPA para operaciones CRUD
   - Métodos personalizados para buscar recordatorios por usuario, estado, y recordatorios pendientes

3. **ReminderService** (`service/ReminderService.java`)
   - Lógica de negocio para gestión de recordatorios
   - Sincronización bidireccional con Firebase Firestore
   - Procesamiento de recordatorios pendientes y envío de notificaciones

4. **ReminderScheduler** (`scheduler/ReminderScheduler.java`)
   - Tarea programada que ejecuta cada minuto
   - Verifica recordatorios pendientes y dispara notificaciones

5. **ReminderController** (`controller/ReminderController.java`)
   - API REST para gestión de recordatorios
   - Endpoints para CRUD y sincronización con Firestore

## Flujo de Funcionamiento

### 1. Creación de Recordatorio

```
Cliente → POST /api/reminders → ReminderController
    ↓
ReminderService.createReminder()
    ↓
Guarda en BD Local → Calcula nextExecution → Sincroniza con Firestore
```

### 2. Ejecución de Recordatorio

```
ReminderScheduler (cada minuto)
    ↓
ReminderService.processDueReminders()
    ↓
Busca reminders con nextExecution <= now
    ↓
Para cada reminder:
    - Envía notificación push via PushNotificationService
    - Marca como triggered
    - Calcula próxima ejecución
    - Actualiza en BD y Firestore
```

## API REST Endpoints

### 1. Crear Recordatorio

**POST** `/api/reminders`

```json
{
  "title": "Alimentar a Luna",
  "message": "Es hora de alimentar a tu mascota",
  "scheduledTime": "09:00",
  "repeatInterval": "DAILY",
  "userId": "firebase-user-id",
  "petId": 1,
  "deviceToken": "fcm-device-token-here",
  "active": true
}
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Reminder created successfully",
  "reminder": {
    "id": 1,
    "firebaseId": "firebase-doc-id",
    "title": "Alimentar a Luna",
    "scheduledTime": "09:00",
    "repeatInterval": "DAILY",
    "nextExecution": "2025-11-18T09:00:00",
    ...
  }
}
```

### 2. Obtener Recordatorios de Usuario

**GET** `/api/reminders/user/{userId}`

**Respuesta:**
```json
{
  "success": true,
  "count": 3,
  "reminders": [...]
}
```

### 3. Obtener Recordatorios Activos de Usuario

**GET** `/api/reminders/user/{userId}/active`

### 4. Obtener Recordatorio por ID

**GET** `/api/reminders/{id}`

### 5. Actualizar Recordatorio

**PUT** `/api/reminders/{id}`

```json
{
  "title": "Pasear a Rocky",
  "scheduledTime": "18:00",
  "repeatInterval": "DAILY",
  "active": true,
  "deviceToken": "fcm-token"
}
```

### 6. Eliminar Recordatorio

**DELETE** `/api/reminders/{id}`

### 7. Sincronizar desde Firestore

**POST** `/api/reminders/sync/{userId}`

Sincroniza los recordatorios desde Firestore a la base de datos local.

## Intervalos de Repetición Disponibles

La enumeración `RepeatInterval` soporta los siguientes valores:

- `ONCE` - Una sola vez (no se repite)
- `DAILY` - Diario
- `WEEKLY` - Semanal
- `MONTHLY` - Mensual
- `YEARLY` - Anual
- `EVERY_HOUR` - Cada hora
- `EVERY_2_HOURS` - Cada 2 horas
- `EVERY_4_HOURS` - Cada 4 horas
- `EVERY_6_HOURS` - Cada 6 horas
- `EVERY_12_HOURS` - Cada 12 horas

## Configuración de Firebase

### Requisitos

1. **Firestore Database** debe estar habilitado en tu proyecto Firebase
2. **Firebase Cloud Messaging (FCM)** debe estar configurado
3. Variable de entorno `GOOGLE_APPLICATION_CREDENTIALS` debe apuntar al archivo de credenciales:

```bash
export GOOGLE_APPLICATION_CREDENTIALS=.secrets/firebase-credentials.json
```

### Estructura en Firestore

Los recordatorios se almacenan en la colección `reminders` con la siguiente estructura:

```
reminders/
  └── {firebaseId}/
      ├── id: Long
      ├── title: String
      ├── message: String
      ├── scheduledTime: String (HH:mm)
      ├── repeatInterval: String (enum name)
      ├── userId: String
      ├── petId: Long (nullable)
      ├── deviceToken: String
      ├── active: Boolean
      ├── lastTriggered: Timestamp
      ├── nextExecution: Timestamp
      ├── createdAt: Timestamp
      └── updatedAt: Timestamp
```

## Sincronización con Firestore

El sistema mantiene sincronización bidireccional:

1. **Local → Firestore**: Automática en cada operación CRUD
2. **Firestore → Local**: Mediante endpoint `/api/reminders/sync/{userId}`

Esto permite que:
- Los cambios en la app móvil se reflejen en el backend
- Los cambios en el backend se reflejen en la app móvil
- Se mantenga consistencia entre ambas bases de datos

## Scheduler

El `ReminderScheduler` ejecuta cada minuto (cron: `0 * * * * *`) y:

1. Busca todos los recordatorios activos con `nextExecution <= now`
2. Para cada recordatorio encontrado:
   - Envía una notificación push al dispositivo del usuario
   - Marca el recordatorio como ejecutado (`lastTriggered = now`)
   - Calcula la próxima ejecución basada en el `repeatInterval`
   - Actualiza el recordatorio en BD local y Firestore

## Ejemplo de Uso Completo

### 1. Crear un recordatorio diario para alimentar mascota

```bash
curl -X POST http://localhost:8080/api/reminders \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Alimentar a Luna",
    "message": "Es hora de alimentar a Luna",
    "scheduledTime": "09:00",
    "repeatInterval": "DAILY",
    "userId": "user123",
    "deviceToken": "fcm-token-xyz",
    "active": true
  }'
```

### 2. El sistema automáticamente:
- Guardará el recordatorio en la BD local
- Lo sincronizará con Firestore
- Calculará que la próxima ejecución es mañana a las 09:00

### 3. Cuando llegue la hora (09:00):
- El scheduler detectará el recordatorio pendiente
- Enviará una notificación push al dispositivo
- Actualizará `nextExecution` al siguiente día a las 09:00

### 4. Consultar recordatorios activos

```bash
curl http://localhost:8080/api/reminders/user/user123/active
```

## Notas Técnicas

- El sistema usa JPA con Single Table Inheritance para las mascotas
- Las fechas/horas se manejan con `LocalTime` y `LocalDateTime` de Java 8+
- La sincronización con Firestore es asíncrona usando `ApiFuture`
- Los tokens FCM deben ser válidos y actualizados desde la app móvil
- El scheduler está configurado para ejecutar cada minuto, pero puede ajustarse según necesidades

## Próximas Mejoras Sugeridas

1. Agregar soporte para zonas horarias específicas por usuario
2. Implementar notificaciones en lote para múltiples dispositivos
3. Agregar análisis de métricas de notificaciones enviadas/recibidas
4. Implementar sistema de prioridades para recordatorios
5. Agregar soporte para recordatorios basados en ubicación
