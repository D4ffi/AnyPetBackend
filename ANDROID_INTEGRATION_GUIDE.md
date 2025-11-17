# Guía de Integración: AnyPet Android App ↔ Backend API

Esta guía te ayudará a conectar tu aplicación Android con el backend de AnyPet para gestionar mascotas, vacunas, recordatorios y almacenar imágenes en AWS S3.

## Tabla de Contenidos

1. [Requisitos Previos](#requisitos-previos)
2. [Configuración de Firebase](#configuración-de-firebase)
3. [Configuración de Retrofit](#configuración-de-retrofit)
4. [Autenticación](#autenticación)
5. [Endpoints Disponibles](#endpoints-disponibles)
   - [Imágenes (S3)](#1-imágenes-s3)
   - [Recordatorios](#2-recordatorios)
   - [Vacunas](#3-vacunas)
6. [Ejemplos de Código Android](#ejemplos-de-código-android)
7. [Manejo de Errores](#manejo-de-errores)
8. [Mejores Prácticas](#mejores-prácticas)

---

## Requisitos Previos

### Backend
- ✅ Backend desplegado y accesible (URL: `https://tu-backend.com` o `http://localhost:8080` para desarrollo)
- ✅ Bucket de AWS S3 configurado (`anypet-images-bucket`)
- ✅ Firebase proyecto configurado con Authentication habilitado
- ✅ Firebase Cloud Messaging (FCM) configurado para notificaciones push

### Android
- Android Studio Arctic Fox o superior
- minSdkVersion 24 (Android 7.0) o superior
- Kotlin 1.8+

---

## Configuración de Firebase

### 1. Agregar Firebase a tu proyecto Android

**a) Descarga el archivo `google-services.json`:**
   - Ve a [Firebase Console](https://console.firebase.google.com/)
   - Selecciona tu proyecto
   - Agrega una app Android
   - Descarga `google-services.json` y colócalo en `app/`

**b) Agrega las dependencias en `build.gradle`:**

```gradle
// Project-level build.gradle
buildscript {
    dependencies {
        classpath 'com.google.gms:google-services:4.4.0'
    }
}

// App-level build.gradle
plugins {
    id 'com.android.application'
    id 'kotlin-android'
    id 'com.google.gms.google-services'
}

dependencies {
    // Firebase Authentication
    implementation 'com.google.firebase:firebase-auth-ktx:22.3.0'

    // Firebase Cloud Messaging (para notificaciones push)
    implementation 'com.google.firebase:firebase-messaging-ktx:23.3.1'

    // Firestore (opcional, para sincronización)
    implementation 'com.google.firebase:firebase-firestore-ktx:24.9.1'

    // Retrofit para llamadas HTTP
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'

    // OkHttp para interceptores
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'

    // Coil para cargar imágenes
    implementation 'io.coil-kt:coil:2.5.0'

    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
}
```

### 2. Configurar Firebase Authentication

```kotlin
// En tu Activity principal o Application class
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }
}
```

---

## Configuración de Retrofit

### 1. Crear el Interceptor de Autenticación

Crea un interceptor que agregue el token de Firebase a todas las peticiones:

```kotlin
// AuthInterceptor.kt
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Obtener el token de Firebase de forma síncrona
        val token = runBlocking {
            try {
                FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()?.token
            } catch (e: Exception) {
                null
            }
        }

        // Agregar el token al header si existe
        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}
```

### 2. Configurar Retrofit

```kotlin
// ApiClient.kt
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "http://localhost:8080/" // Cambiar en producción

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
```

---

## Autenticación

### Obtener el Token de Firebase

El token se obtiene automáticamente a través del `AuthInterceptor`, pero puedes obtenerlo manualmente:

```kotlin
suspend fun getFirebaseToken(): String? {
    return try {
        FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()?.token
    } catch (e: Exception) {
        Log.e("Auth", "Error getting token", e)
        null
    }
}
```

### Autenticación del Usuario

```kotlin
// Login con email y password
suspend fun loginUser(email: String, password: String): Boolean {
    return try {
        val result = FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(email, password)
            .await()
        result.user != null
    } catch (e: Exception) {
        Log.e("Auth", "Login failed", e)
        false
    }
}

// Obtener el User ID actual
fun getCurrentUserId(): String? {
    return FirebaseAuth.getInstance().currentUser?.uid
}
```

---

## Endpoints Disponibles

### 1. Imágenes (S3)

#### 1.1 Subir Imagen de Perfil de Mascota

**Endpoint:** `POST /api/images/pet/{petId}/profile`

**Headers:**
- `Authorization: Bearer {firebase-token}`
- `Content-Type: multipart/form-data`

**Parámetros:**
- `petId`: ID de la mascota (Path parameter)
- `file`: Archivo de imagen (Multipart)

**Respuesta:**
```json
{
  "success": true,
  "message": "Pet profile image uploaded successfully",
  "imageUrl": "https://anypet-images-bucket.s3.amazonaws.com/users/{userId}/pets/profiles/{petId}_{timestamp}_{uuid}.jpg",
  "thumbnailUrl": "https://anypet-images-bucket.s3.amazonaws.com/users/{userId}/pets/thumbnails/{petId}_{timestamp}_{uuid}.jpg",
  "entityId": 1,
  "imageType": "PET_PROFILE"
}
```

**Código Android:**

```kotlin
// ApiService.kt
interface ApiService {
    @Multipart
    @POST("api/images/pet/{petId}/profile")
    suspend fun uploadPetProfileImage(
        @Path("petId") petId: Long,
        @Part file: MultipartBody.Part
    ): ImageUploadResponse
}

// Uso
suspend fun uploadPetImage(petId: Long, imageUri: Uri) {
    val file = uriToFile(imageUri) // Helper para convertir Uri a File
    val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
    val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestBody)

    try {
        val response = apiService.uploadPetProfileImage(petId, multipartBody)
        if (response.success) {
            Log.d("Upload", "Image URL: ${response.imageUrl}")
            // Guardar la URL en tu base de datos local o actualizar UI
        }
    } catch (e: Exception) {
        Log.e("Upload", "Error uploading image", e)
    }
}
```

#### 1.2 Subir Imagen de Lote de Vacuna

**Endpoint:** `POST /api/images/vaccine/{vaccinationRecordId}/batch`

**Headers:**
- `Authorization: Bearer {firebase-token}`
- `Content-Type: multipart/form-data`

**Parámetros:**
- `vaccinationRecordId`: ID del registro de vacunación (Path parameter)
- `file`: Archivo de imagen del lote (Multipart)

**Código Android:**

```kotlin
@Multipart
@POST("api/images/vaccine/{vaccinationRecordId}/batch")
suspend fun uploadVaccineBatchImage(
    @Path("vaccinationRecordId") vaccinationRecordId: Long,
    @Part file: MultipartBody.Part
): ImageUploadResponse
```

#### 1.3 Eliminar Imagen

**Endpoint:** `DELETE /api/images?url={imageUrl}`

**Headers:**
- `Authorization: Bearer {firebase-token}`

**Código Android:**

```kotlin
@DELETE("api/images")
suspend fun deleteImage(
    @Query("url") imageUrl: String
): DeleteImageResponse
```

#### 1.4 Verificar si una Imagen Existe

**Endpoint:** `GET /api/images/exists?url={imageUrl}`

```kotlin
@GET("api/images/exists")
suspend fun checkImageExists(
    @Query("url") imageUrl: String
): ImageExistsResponse
```

---

### 2. Recordatorios

#### 2.1 Crear Recordatorio

**Endpoint:** `POST /api/reminders`

**Headers:**
- `Authorization: Bearer {firebase-token}`
- `Content-Type: application/json`

**Body:**
```json
{
  "title": "Alimentar a Luna",
  "message": "Es hora de alimentar a tu mascota",
  "scheduledTime": "09:00",
  "repeatInterval": "DAILY",
  "userId": "firebase-user-id",
  "petId": 1,
  "deviceToken": "fcm-device-token",
  "active": true
}
```

**Intervalos disponibles:**
- `ONCE` - Una vez
- `DAILY` - Diario
- `WEEKLY` - Semanal
- `MONTHLY` - Mensual
- `YEARLY` - Anual
- `EVERY_HOUR` - Cada hora
- `EVERY_2_HOURS` - Cada 2 horas
- `EVERY_4_HOURS` - Cada 4 horas
- `EVERY_6_HOURS` - Cada 6 horas
- `EVERY_12_HOURS` - Cada 12 horas

**Código Android:**

```kotlin
// Models.kt
data class ReminderRequest(
    val title: String,
    val message: String?,
    val scheduledTime: String, // "HH:mm" format
    val repeatInterval: String,
    val userId: String,
    val petId: Long?,
    val deviceToken: String,
    val active: Boolean = true
)

data class ReminderResponse(
    val success: Boolean,
    val message: String,
    val reminder: Reminder?
)

data class Reminder(
    val id: Long,
    val title: String,
    val message: String?,
    val scheduledTime: String,
    val repeatInterval: String,
    val userId: String,
    val petId: Long?,
    val active: Boolean,
    val createdAt: String
)

// ApiService.kt
@POST("api/reminders")
suspend fun createReminder(
    @Body request: ReminderRequest
): ReminderResponse

// Uso
suspend fun createFeedingReminder(petId: Long) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    // Obtener FCM token
    val fcmToken = FirebaseMessaging.getInstance().token.await()

    val reminder = ReminderRequest(
        title = "Alimentar a mi mascota",
        message = "Es hora de alimentar a tu mascota",
        scheduledTime = "09:00",
        repeatInterval = "DAILY",
        userId = userId,
        petId = petId,
        deviceToken = fcmToken,
        active = true
    )

    try {
        val response = apiService.createReminder(reminder)
        if (response.success) {
            Log.d("Reminder", "Recordatorio creado: ${response.reminder?.id}")
        }
    } catch (e: Exception) {
        Log.e("Reminder", "Error creating reminder", e)
    }
}
```

#### 2.2 Obtener Recordatorios del Usuario

**Endpoint:** `GET /api/reminders/user/{userId}`

```kotlin
@GET("api/reminders/user/{userId}")
suspend fun getRemindersByUserId(
    @Path("userId") userId: String
): RemindersListResponse

data class RemindersListResponse(
    val success: Boolean,
    val count: Int,
    val reminders: List<Reminder>
)
```

#### 2.3 Obtener Solo Recordatorios Activos

**Endpoint:** `GET /api/reminders/user/{userId}/active`

```kotlin
@GET("api/reminders/user/{userId}/active")
suspend fun getActiveReminders(
    @Path("userId") userId: String
): RemindersListResponse
```

#### 2.4 Actualizar Recordatorio

**Endpoint:** `PUT /api/reminders/{id}`

```kotlin
@PUT("api/reminders/{id}")
suspend fun updateReminder(
    @Path("id") reminderId: Long,
    @Body request: ReminderRequest
): ReminderResponse
```

#### 2.5 Eliminar Recordatorio

**Endpoint:** `DELETE /api/reminders/{id}`

```kotlin
@DELETE("api/reminders/{id}")
suspend fun deleteReminder(
    @Path("id") reminderId: Long
): GenericResponse
```

#### 2.6 Sincronizar desde Firestore

**Endpoint:** `POST /api/reminders/sync/{userId}`

```kotlin
@POST("api/reminders/sync/{userId}")
suspend fun syncRemindersFromFirestore(
    @Path("userId") userId: String
): GenericResponse
```

---

### 3. Vacunas

#### 3.1 Obtener Todas las Vacunas

**Endpoint:** `GET /api/vaccines`

```kotlin
@GET("api/vaccines")
suspend fun getAllVaccines(): List<Vaccine>

data class Vaccine(
    val id: Long,
    val name: String,
    val description: String?,
    val petType: String, // "CAT", "DOG", etc.
    val isCore: Boolean,
    val recommendedAgeWeeks: Int?,
    val durationMonths: Int?
)
```

#### 3.2 Obtener Vacunas por Tipo de Mascota

**Endpoint:** `GET /api/vaccines/pet-type/{petType}`

```kotlin
@GET("api/vaccines/pet-type/{petType}")
suspend fun getVaccinesByPetType(
    @Path("petType") petType: String // "CAT", "DOG", "BIRD", etc.
): List<Vaccine>

// Uso
suspend fun loadVaccinesForCat() {
    val vaccines = apiService.getVaccinesByPetType("CAT")
    // Mostrar en UI
}
```

#### 3.3 Obtener Vacunas Esenciales (Core)

**Endpoint:** `GET /api/vaccines/pet-type/{petType}/core`

```kotlin
@GET("api/vaccines/pet-type/{petType}/core")
suspend fun getCoreVaccines(
    @Path("petType") petType: String
): List<Vaccine>
```

#### 3.4 Obtener Estadísticas de Vacunas

**Endpoint:** `GET /api/vaccines/statistics/{petType}`

```kotlin
@GET("api/vaccines/statistics/{petType}")
suspend fun getVaccineStatistics(
    @Path("petType") petType: String
): VaccineStatistics

data class VaccineStatistics(
    val petType: String,
    val totalVaccines: Long,
    val coreVaccines: Long,
    val optionalVaccines: Long,
    val averageRecommendedAgeWeeks: Double
)
```

#### 3.5 Crear Nueva Vacuna (Admin)

**Endpoint:** `POST /api/vaccines`

```kotlin
@POST("api/vaccines")
suspend fun createVaccine(
    @Body vaccine: Vaccine
): Vaccine
```

---

## Ejemplos de Código Android

### Ejemplo Completo: Subir Imagen de Perfil de Mascota

```kotlin
class PetProfileViewModel : ViewModel() {
    private val apiService = ApiClient.retrofit.create(ApiService::class.java)

    fun uploadPetProfileImage(context: Context, petId: Long, imageUri: Uri) {
        viewModelScope.launch {
            try {
                // 1. Convertir Uri a File
                val file = createTempFileFromUri(context, imageUri)

                // 2. Crear RequestBody y MultipartBody
                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                val multipartBody = MultipartBody.Part.createFormData(
                    "file",
                    file.name,
                    requestBody
                )

                // 3. Subir imagen
                val response = apiService.uploadPetProfileImage(petId, multipartBody)

                // 4. Manejar respuesta
                if (response.success) {
                    _imageUrl.value = response.imageUrl
                    _thumbnailUrl.value = response.thumbnailUrl

                    // 5. Cargar imagen con Coil
                    loadImageIntoView(response.imageUrl)
                } else {
                    _error.value = response.message
                }

                // 6. Limpiar archivo temporal
                file.delete()

            } catch (e: Exception) {
                _error.value = "Error al subir imagen: ${e.message}"
                Log.e("Upload", "Error", e)
            }
        }
    }

    private fun createTempFileFromUri(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
        tempFile.outputStream().use { outputStream ->
            inputStream?.copyTo(outputStream)
        }
        return tempFile
    }
}
```

### Ejemplo: Cargar Imagen desde S3 con Coil

```kotlin
import coil.load
import coil.transform.CircleCropTransformation

fun ImageView.loadPetProfile(imageUrl: String?) {
    this.load(imageUrl) {
        crossfade(true)
        placeholder(R.drawable.placeholder_pet)
        error(R.drawable.error_image)
        transformations(CircleCropTransformation())
    }
}

// Uso
petImageView.loadPetProfile(pet.profileImageUrl)
```

### Ejemplo: Crear Recordatorio con Notificaciones

```kotlin
class ReminderManager(private val context: Context) {
    private val apiService = ApiClient.retrofit.create(ApiService::class.java)

    suspend fun createDailyFeedingReminder(petName: String, time: String): Boolean {
        return try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return false
            val fcmToken = FirebaseMessaging.getInstance().token.await()

            val reminder = ReminderRequest(
                title = "Alimentar a $petName",
                message = "Es hora de alimentar a tu mascota",
                scheduledTime = time,
                repeatInterval = "DAILY",
                userId = userId,
                petId = null,
                deviceToken = fcmToken,
                active = true
            )

            val response = apiService.createReminder(reminder)
            response.success
        } catch (e: Exception) {
            Log.e("ReminderManager", "Error creating reminder", e)
            false
        }
    }
}
```

### Ejemplo: Listar Vacunas de un Gato

```kotlin
class VaccineListViewModel : ViewModel() {
    private val apiService = ApiClient.retrofit.create(ApiService::class.java)
    private val _vaccines = MutableLiveData<List<Vaccine>>()
    val vaccines: LiveData<List<Vaccine>> = _vaccines

    fun loadVaccinesForCat() {
        viewModelScope.launch {
            try {
                val allVaccines = apiService.getVaccinesByPetType("CAT")
                _vaccines.value = allVaccines
            } catch (e: Exception) {
                Log.e("Vaccines", "Error loading vaccines", e)
            }
        }
    }

    fun loadOnlyCoreVaccines() {
        viewModelScope.launch {
            try {
                val coreVaccines = apiService.getCoreVaccines("CAT")
                _vaccines.value = coreVaccines
            } catch (e: Exception) {
                Log.e("Vaccines", "Error loading core vaccines", e)
            }
        }
    }
}
```

---

## Manejo de Errores

### Códigos de Estado HTTP

| Código | Significado | Acción |
|--------|-------------|--------|
| 200 | OK | Operación exitosa |
| 201 | Created | Recurso creado exitosamente |
| 400 | Bad Request | Datos inválidos, verificar el payload |
| 401 | Unauthorized | Token de Firebase inválido o expirado |
| 404 | Not Found | Recurso no encontrado |
| 500 | Internal Server Error | Error del servidor, reintentar |

### Interceptor de Manejo de Errores

```kotlin
class ErrorHandlingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        when (response.code) {
            401 -> {
                // Token expirado, refrescar
                Log.w("Auth", "Token expired, need to refresh")
                // Implementar lógica de refresh
            }
            500 -> {
                Log.e("Server", "Server error: ${response.message}")
            }
        }

        return response
    }
}
```

### Manejo de Errores en Retrofit

```kotlin
suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
    return try {
        Result.success(apiCall())
    } catch (e: HttpException) {
        Log.e("API", "HTTP error: ${e.code()}", e)
        Result.failure(e)
    } catch (e: IOException) {
        Log.e("API", "Network error", e)
        Result.failure(e)
    } catch (e: Exception) {
        Log.e("API", "Unknown error", e)
        Result.failure(e)
    }
}

// Uso
val result = safeApiCall { apiService.getAllVaccines() }
result.onSuccess { vaccines ->
    // Mostrar vacunas
}.onFailure { error ->
    // Mostrar error
}
```

---

## Mejores Prácticas

### 1. Seguridad
- ✅ NUNCA almacenes tokens en SharedPreferences sin encriptar
- ✅ Usa HTTPS en producción
- ✅ Valida siempre el token antes de hacer llamadas
- ✅ Implementa timeout adecuados (30 segundos recomendado)

### 2. Gestión de Imágenes
- ✅ Comprime imágenes antes de subirlas (el backend ya comprime, pero reduce tiempo de carga)
- ✅ Usa thumbnails para listas y imágenes completas para detalles
- ✅ Implementa cache de imágenes con Coil
- ✅ Limpia archivos temporales después de subir

```kotlin
fun compressImage(context: Context, uri: Uri, quality: Int = 80): File {
    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    val file = File.createTempFile("compressed", ".jpg", context.cacheDir)
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
    }
    return file
}
```

### 3. Notificaciones Push
- ✅ Solicita permisos de notificaciones en Android 13+
- ✅ Actualiza el FCM token cuando cambie
- ✅ Maneja notificaciones tanto en foreground como background

```kotlin
// MyFirebaseMessagingService.kt
class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        // Actualizar token en el servidor
        updateTokenOnServer(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // Mostrar notificación del recordatorio
        showReminderNotification(message)
    }
}
```

### 4. Manejo de Conectividad
- ✅ Verifica conectividad antes de hacer llamadas
- ✅ Implementa reintentos con backoff exponencial
- ✅ Almacena datos localmente con Room para offline-first

```kotlin
fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
```

### 5. Caché y Optimización
- ✅ Implementa cache con Room para datos frecuentes (vacunas, recordatorios)
- ✅ Usa WorkManager para sincronización en background
- ✅ Limita el número de reintentos

---

## Configuración de Producción

### Variables de Entorno

Crea un archivo `local.properties` o usa BuildConfig:

```gradle
// build.gradle (app)
android {
    defaultConfig {
        buildConfigField "String", "API_BASE_URL", "\"https://tu-backend.elasticbeanstalk.com/\""
        buildConfigField "String", "S3_BUCKET", "\"anypet-images-bucket\""
    }
}

// Uso en código
object ApiClient {
    private const val BASE_URL = BuildConfig.API_BASE_URL
}
```

---

## Estructura de Almacenamiento en S3

Las imágenes se organizan por usuario para máxima seguridad:

```
anypet-images-bucket/
└── users/
    └── {firebase-user-id}/
        ├── pets/
        │   ├── profiles/
        │   │   └── {petId}_{timestamp}_{uuid}.jpg
        │   └── thumbnails/
        │       └── {petId}_{timestamp}_{uuid}.jpg
        └── vaccines/
            ├── batches/
            │   └── {vaccinationRecordId}_{timestamp}_{uuid}.jpg
            └── thumbnails/
                └── {vaccinationRecordId}_{timestamp}_{uuid}.jpg
```

---

## Soporte

Si encuentras problemas:
1. Verifica que Firebase esté correctamente configurado
2. Confirma que el token de autenticación se esté enviando
3. Revisa los logs del backend para más detalles
4. Asegúrate de que el bucket S3 tenga los permisos correctos

---

## Checklist de Integración

- [ ] Firebase agregado al proyecto Android
- [ ] Dependencias de Retrofit y OkHttp agregadas
- [ ] AuthInterceptor implementado
- [ ] ApiService interface creada con todos los endpoints
- [ ] Manejo de errores implementado
- [ ] FCM configurado para notificaciones
- [ ] Coil configurado para carga de imágenes
- [ ] Permisos de internet y almacenamiento agregados al Manifest
- [ ] URL de producción configurada en BuildConfig
- [ ] Probado subir imagen de mascota
- [ ] Probado crear recordatorio
- [ ] Probado obtener lista de vacunas

---

## Ejemplo AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application
        android:usesCleartextTraffic="true"> <!-- Solo para desarrollo local -->

        <service
            android:name=".MyFirebaseMessagingService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>

    </application>
</manifest>
```

---

¡Listo! Con esta guía deberías poder integrar completamente tu app Android con el backend de AnyPet.
