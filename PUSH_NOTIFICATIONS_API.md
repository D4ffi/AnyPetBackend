# Push Notifications API

Sistema de notificaciones push integrado con Firebase Cloud Messaging (FCM).

## Estructura de Archivos Creados

```
src/main/java/com/bydaffi/anypetbackend/
├── controller/
│   └── PushNotificationController.java   # REST endpoints
├── service/
│   └── PushNotificationService.java      # Lógica de negocio FCM
└── dto/
    └── PushNotificationRequest.java      # DTO para requests
```

## Endpoints Disponibles

### 1. Enviar Notificación Individual

**POST** `/api/notifications/send`

Envía una notificación push a un dispositivo específico.

#### Ejemplos de Uso:

**Notificación específica de mascota:**
```json
{
  "petName": "Luna",
  "message": "tiene hambre",
  "token": "fcm-device-token-aqui",
  "title": "Alerta de Mascota"
}
```
Resultado: "Luna tiene hambre"

**Otra notificación de mascota:**
```json
{
  "petName": "Rocky",
  "message": "debería salir a pasear pronto",
  "token": "fcm-device-token-aqui"
}
```
Resultado: "Rocky debería salir a pasear pronto"

**Notificación general (sin nombre de mascota):**
```json
{
  "message": "Es hora de alimentar a tus mascotas",
  "token": "fcm-device-token-aqui"
}
```
Resultado: "Es hora de alimentar a tus mascotas"

#### Respuesta Exitosa:
```json
{
  "success": true,
  "message": "Notification sent successfully",
  "messageId": "projects/your-project/messages/1234567890"
}
```

#### Respuesta de Error:
```json
{
  "success": false,
  "message": "Failed to send notification: Invalid registration token",
  "errorCode": "invalid-registration-token"
}
```

---

### 2. Enviar Notificaciones en Lote

**POST** `/api/notifications/send/batch`

Envía la misma notificación a múltiples dispositivos.

#### Ejemplo de Request:
```json
{
  "petName": "Luna",
  "message": "tiene hambre",
  "title": "Alerta de Mascota",
  "tokens": [
    "fcm-token-device-1",
    "fcm-token-device-2",
    "fcm-token-device-3"
  ]
}
```

#### Respuesta:
```json
{
  "success": true,
  "message": "Batch notifications sent",
  "successCount": 2,
  "failureCount": 1,
  "totalCount": 3
}
```

---

### 3. Health Check

**GET** `/api/notifications/health`

Verifica que el servicio de notificaciones esté funcionando.

#### Respuesta:
```json
{
  "status": "ok",
  "service": "Push Notification Service"
}
```

---

## Características de la Implementación

### Campos del DTO (PushNotificationRequest)

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `petName` | String | No | Nombre de la mascota (opcional) |
| `message` | String | Sí | Mensaje de la notificación |
| `token` | String | Sí | Token FCM del dispositivo |
| `title` | String | No | Título de la notificación (default: "AnyPet") |

### Funcionalidades del Servicio

1. **Construcción automática de mensajes:**
   - Si `petName` está presente: combina "petName + message"
   - Si `petName` es null: usa solo el `message`

2. **Configuración multiplataforma:**
   - **Android:** Alta prioridad, sonido por defecto, color #FF6B6B
   - **iOS:** Sonido por defecto, badge count

3. **Payload de datos:**
   - Incluye `petName`, `message` y `timestamp`
   - Útil para procesamiento en la app móvil

4. **Logging:**
   - Usa SLF4J para registrar éxitos y errores
   - IDs de mensaje para seguimiento

---

## Configuración Requerida

### Firebase Setup

Asegúrate de que Firebase está configurado correctamente:

1. El archivo `firebase-credentials.json` debe estar en `.secrets/`
2. Variable de entorno configurada:
   ```bash
   export GOOGLE_APPLICATION_CREDENTIALS=.secrets/firebase-credentials.json
   ```

3. `FirebaseConfig.java` debe inicializar correctamente el SDK

---

## Ejemplos de Integración

### cURL

```bash
# Notificación individual
curl -X POST http://localhost:8080/api/notifications/send \
  -H "Content-Type: application/json" \
  -d '{
    "petName": "Luna",
    "message": "tiene hambre",
    "token": "your-fcm-token",
    "title": "Alerta"
  }'

# Notificación en lote
curl -X POST http://localhost:8080/api/notifications/send/batch \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Es hora de alimentar a tus mascotas",
    "tokens": ["token1", "token2"]
  }'
```

### JavaScript (Frontend)

```javascript
// Enviar notificación individual
const sendNotification = async (petName, message, fcmToken) => {
  const response = await fetch('http://localhost:8080/api/notifications/send', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      petName: petName,
      message: message,
      token: fcmToken,
      title: 'AnyPet'
    })
  });

  return await response.json();
};

// Uso
sendNotification('Luna', 'tiene hambre', 'fcm-device-token')
  .then(result => console.log('Notificación enviada:', result))
  .catch(error => console.error('Error:', error));
```

---

## Manejo de Errores

### Errores Comunes de FCM

| Error Code | Descripción | Solución |
|------------|-------------|----------|
| `invalid-registration-token` | Token FCM inválido | Verificar que el token sea válido |
| `registration-token-not-registered` | Token no registrado | El usuario desinstaló la app |
| `message-rate-exceeded` | Límite de tasa excedido | Implementar rate limiting |
| `invalid-argument` | Argumentos inválidos | Verificar formato de request |

### Logging

Todos los eventos se registran con SLF4J:
```
[INFO] Successfully sent push notification. Message ID: projects/...
[ERROR] Error sending push notification: Invalid registration token
```

---

## Próximos Pasos

1. **Implementar scheduling:** Usar `@Scheduled` para notificaciones automáticas
2. **Integrar con Pet entities:** Enviar notificaciones basadas en horarios de alimentación
3. **Topic subscriptions:** Permitir suscripciones a temas (ej: "all-pets", "cat-lovers")
4. **Notificaciones basadas en eventos:** Disparar cuando se creen vacunas, recordatorios, etc.

---

## Testing

Para probar el servicio localmente:

1. Obtén un token FCM válido desde tu app móvil
2. Usa Postman o cURL para enviar requests
3. Verifica logs en la consola de Spring Boot
4. Revisa la consola de Firebase para estadísticas de entrega
