# 📸 Sistema de Almacenamiento de Imágenes en S3 - AnyPetBackend

## 📋 Índice
- [Visión General](#visión-general)
- [Configuración AWS S3](#configuración-aws-s3)
- [Arquitectura](#arquitectura)
- [API Endpoints](#api-endpoints)
- [Ejemplos de Uso](#ejemplos-de-uso)
- [Estructura de Almacenamiento S3](#estructura-de-almacenamiento-s3)
- [Compresión de Imágenes](#compresión-de-imágenes)

---

## 🎯 Visión General

El sistema permite a los usuarios:
1. **Subir foto de perfil para sus mascotas** - Almacenada en S3 con compresión automática
2. **Subir foto del lote/batch de vacunas** - Para registrar el número de lote del vial/caja

Características:
- ✅ Compresión automática de imágenes (reduce tamaño hasta 85% de calidad)
- ✅ Generación automática de thumbnails (200x200px)
- ✅ Soporte para JPG, PNG, GIF
- ✅ Validación de tamaño máximo (10MB por defecto)
- ✅ Nombres únicos con timestamp y UUID
- ✅ Almacenamiento seguro en AWS S3

---

## ⚙️ Configuración AWS S3

### 🔴 IMPORTANTE: Configurar Credenciales de Producción

Los valores actuales en `application.properties` son **MOCK/PLACEHOLDER**. Debes reemplazarlos con tus credenciales reales de AWS.

### Paso 1: Crear Bucket S3

1. Accede a AWS Console → S3
2. Crea un nuevo bucket (ejemplo: `anypet-images-production`)
3. Configuración recomendada:
   - **Region**: Elige la más cercana a tus usuarios (ej: `us-east-1`)
   - **Block Public Access**: Desactivar si quieres URLs públicas directas
   - **Versioning**: Opcional (recomendado para backup)
   - **Encryption**: Habilitar AES-256

### Paso 2: Configurar CORS (si las imágenes se acceden desde el frontend)

En la configuración del bucket S3, agrega esta política CORS:

```json
[
    {
        "AllowedHeaders": ["*"],
        "AllowedMethods": ["GET", "PUT", "POST", "DELETE"],
        "AllowedOrigins": ["*"],
        "ExposeHeaders": ["ETag"]
    }
]
```

### Paso 3: Crear Usuario IAM

1. AWS Console → IAM → Users → Create User
2. Nombre: `anypet-backend-s3-user`
3. Acceso: Programmatic access
4. Permisos: Adjuntar política `AmazonS3FullAccess` o crear política personalizada:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "s3:PutObject",
                "s3:GetObject",
                "s3:DeleteObject",
                "s3:ListBucket"
            ],
            "Resource": [
                "arn:aws:s3:::anypet-images-production/*",
                "arn:aws:s3:::anypet-images-production"
            ]
        }
    ]
}
```

5. **Guarda las credenciales** generadas (Access Key ID y Secret Access Key)

### Paso 4: Actualizar `application.properties`

Abre el archivo `src/main/resources/application.properties` y **REEMPLAZA** estos valores:

```properties
# ========================================
# AWS S3 Configuration - CHANGE THESE VALUES IN PRODUCTION
# ========================================
# TODO: Replace these mock values with your actual AWS credentials
aws.s3.access-key=YOUR_AWS_ACCESS_KEY_HERE          # ⚠️ CAMBIAR: Tu Access Key ID
aws.s3.secret-key=YOUR_AWS_SECRET_KEY_HERE          # ⚠️ CAMBIAR: Tu Secret Access Key
aws.s3.region=us-east-1                             # ⚠️ CAMBIAR: Tu región (ej: us-west-2, eu-west-1)
aws.s3.bucket-name=anypet-images-bucket             # ⚠️ CAMBIAR: Nombre de tu bucket

# Image upload settings (opcionales, puedes ajustarlos)
app.image.max-size-mb=10
app.image.compression-quality=0.85
app.image.max-width=1920
app.image.max-height=1920
app.image.thumbnail-size=200
```

### Valores a Cambiar

| Propiedad | Descripción | Ejemplo |
|-----------|-------------|---------|
| `aws.s3.access-key` | Access Key ID del usuario IAM | `AKIAIOSFODNN7EXAMPLE` |
| `aws.s3.secret-key` | Secret Access Key del usuario IAM | `wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY` |
| `aws.s3.region` | Región donde creaste el bucket | `us-east-1`, `us-west-2`, `eu-west-1` |
| `aws.s3.bucket-name` | Nombre exacto de tu bucket S3 | `anypet-images-production` |

### ⚠️ Seguridad: Variables de Entorno (Recomendado)

En lugar de poner las credenciales directamente en `application.properties`, usa variables de entorno:

```properties
# application.properties
aws.s3.access-key=${AWS_ACCESS_KEY}
aws.s3.secret-key=${AWS_SECRET_KEY}
aws.s3.region=${AWS_REGION:us-east-1}
aws.s3.bucket-name=${AWS_BUCKET_NAME}
```

Luego configura las variables de entorno en tu servidor:

```bash
export AWS_ACCESS_KEY=tu_access_key_aqui
export AWS_SECRET_KEY=tu_secret_key_aqui
export AWS_REGION=us-east-1
export AWS_BUCKET_NAME=anypet-images-production
```

---

## 🏗️ Arquitectura

### Componentes Implementados

1. **S3Config** - `config/S3Config.java`
   - Configura el cliente S3 de AWS SDK v2
   - Lee credenciales de `application.properties`

2. **ImageCompressionService** - `service/ImageCompressionService.java`
   - Comprime imágenes con calidad configurable (default 85%)
   - Redimensiona si excede dimensiones máximas (1920x1920)
   - Genera thumbnails cuadrados (200x200)
   - Soporte para JPG, PNG, GIF

3. **S3Service** - `service/S3Service.java`
   - Sube imágenes comprimidas a S3
   - Genera nombres únicos con timestamp + UUID
   - Elimina imágenes
   - Obtiene metadata
   - Valida tamaño y formato

4. **ImageUploadController** - `controller/ImageUploadController.java`
   - API REST para subir/eliminar imágenes
   - Endpoints separados para mascotas y vacunas

5. **Entidades Actualizadas**
   - **Pet**: Campos `profileImageUrl` y `profileThumbnailUrl`
   - **VaccinationRecord**: Campos `batchImageUrl`, `batchThumbnailUrl`, `batchNumber`

---

## 🔌 API Endpoints

### Base URL: `http://localhost:8080/api/images`

| Método | Endpoint | Descripción | Request | Response |
|--------|----------|-------------|---------|----------|
| POST | `/pet/{petId}/profile` | Subir foto de perfil de mascota | `multipart/form-data` | Imagen + thumbnail URLs |
| POST | `/vaccine/{vaccinationRecordId}/batch` | Subir foto de lote de vacuna | `multipart/form-data` | Imagen + thumbnail URLs |
| DELETE | `/?url={s3Url}` | Eliminar imagen de S3 | Query param `url` | Success message |
| GET | `/metadata?url={s3Url}` | Obtener metadata de imagen | Query param `url` | Size, type, date |
| GET | `/exists?url={s3Url}` | Verificar si imagen existe | Query param `url` | `{"exists": true/false}` |

---

## 📝 Ejemplos de Uso

### 1️⃣ Subir Foto de Perfil de Mascota

**Frontend (JavaScript/Fetch):**

```javascript
const formData = new FormData();
formData.append('file', imageFile); // File object from <input type="file">

const response = await fetch('http://localhost:8080/api/images/pet/123/profile', {
  method: 'POST',
  body: formData
});

const result = await response.json();
console.log(result);
```

**Respuesta exitosa:**

```json
{
  "success": true,
  "message": "Pet profile image uploaded successfully",
  "imageUrl": "https://anypet-images-bucket.s3.amazonaws.com/pets/profiles/123_20250117_143022_a1b2c3d4.jpg",
  "thumbnailUrl": "https://anypet-images-bucket.s3.amazonaws.com/pets/thumbnails/123_20250117_143022_e5f6g7h8.jpg",
  "entityId": 123,
  "imageType": "PET_PROFILE"
}
```

**cURL:**

```bash
curl -X POST http://localhost:8080/api/images/pet/123/profile \
  -F "file=@/path/to/image.jpg"
```

### 2️⃣ Subir Foto de Lote de Vacuna

**Frontend (JavaScript/Fetch):**

```javascript
const formData = new FormData();
formData.append('file', vaccineImageFile);

const response = await fetch('http://localhost:8080/api/images/vaccine/456/batch', {
  method: 'POST',
  body: formData
});

const result = await response.json();
console.log(result);
```

**Respuesta exitosa:**

```json
{
  "success": true,
  "message": "Vaccine batch image uploaded successfully",
  "imageUrl": "https://anypet-images-bucket.s3.amazonaws.com/vaccines/batches/456_20250117_143530_i9j0k1l2.jpg",
  "thumbnailUrl": "https://anypet-images-bucket.s3.amazonaws.com/vaccines/thumbnails/456_20250117_143530_m3n4o5p6.jpg",
  "entityId": 456,
  "imageType": "VACCINE_BATCH"
}
```

**cURL:**

```bash
curl -X POST http://localhost:8080/api/images/vaccine/456/batch \
  -F "file=@/path/to/vaccine_lot.jpg"
```

### 3️⃣ Eliminar Imagen

```bash
curl -X DELETE "http://localhost:8080/api/images?url=https://anypet-images-bucket.s3.amazonaws.com/pets/profiles/123_20250117_143022_a1b2c3d4.jpg"
```

**Respuesta:**

```json
{
  "success": true,
  "message": "Image deleted successfully",
  "deletedUrl": "https://anypet-images-bucket.s3.amazonaws.com/pets/profiles/123_20250117_143022_a1b2c3d4.jpg"
}
```

### 4️⃣ Verificar si Imagen Existe

```bash
curl "http://localhost:8080/api/images/exists?url=https://anypet-images-bucket.s3.amazonaws.com/pets/profiles/123_20250117_143022_a1b2c3d4.jpg"
```

**Respuesta:**

```json
{
  "exists": true
}
```

### 5️⃣ Obtener Metadata de Imagen

```bash
curl "http://localhost:8080/api/images/metadata?url=https://anypet-images-bucket.s3.amazonaws.com/pets/profiles/123_20250117_143022_a1b2c3d4.jpg"
```

**Respuesta:**

```json
{
  "key": "pets/profiles/123_20250117_143022_a1b2c3d4.jpg",
  "size": 245678,
  "contentType": "image/jpeg",
  "lastModified": "2025-01-17T14:30:22Z"
}
```

### 6️⃣ Flujo Completo: Crear Mascota + Subir Foto

```javascript
// 1. Crear mascota (API de Pet)
const petData = {
  name: "Firulais",
  race: "Labrador",
  age: 3,
  weight: 25.5,
  healthStatus: true
};

const petResponse = await fetch('http://localhost:8080/api/pets', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(petData)
});

const pet = await petResponse.json();
const petId = pet.id;

// 2. Subir foto de perfil
const formData = new FormData();
formData.append('file', profileImage);

const imageResponse = await fetch(`http://localhost:8080/api/images/pet/${petId}/profile`, {
  method: 'POST',
  body: formData
});

const imageResult = await imageResponse.json();

// 3. Actualizar mascota con URLs de imágenes
const updateData = {
  ...petData,
  profileImageUrl: imageResult.imageUrl,
  profileThumbnailUrl: imageResult.thumbnailUrl
};

await fetch(`http://localhost:8080/api/pets/${petId}`, {
  method: 'PUT',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(updateData)
});
```

---

## 📁 Estructura de Almacenamiento S3

Las imágenes se organizan en carpetas dentro del bucket:

```
anypet-images-bucket/
├── pets/
│   ├── profiles/
│   │   ├── 1_20250117_120000_a1b2c3d4.jpg
│   │   ├── 2_20250117_120530_e5f6g7h8.jpg
│   │   └── ...
│   └── thumbnails/
│       ├── 1_20250117_120000_i9j0k1l2.jpg
│       ├── 2_20250117_120530_m3n4o5p6.jpg
│       └── ...
└── vaccines/
    ├── batches/
    │   ├── 10_20250117_133000_q7r8s9t0.jpg
    │   ├── 11_20250117_133200_u1v2w3x4.jpg
    │   └── ...
    └── thumbnails/
        ├── 10_20250117_133000_y5z6a7b8.jpg
        ├── 11_20250117_133200_c9d0e1f2.jpg
        └── ...
```

### Formato de Nombre de Archivo

```
{entityId}_{timestamp}_{uuid}.{extension}
```

Ejemplo: `123_20250117_143022_a1b2c3d4.jpg`

- `123`: ID de la mascota o registro de vacunación
- `20250117_143022`: Fecha y hora (YYYYMMDD_HHmmss)
- `a1b2c3d4`: UUID corto (8 caracteres) para unicidad
- `jpg`: Extensión del archivo original

---

## 🗜️ Compresión de Imágenes

### Configuración de Compresión

| Parámetro | Valor Default | Descripción |
|-----------|---------------|-------------|
| `app.image.compression-quality` | `0.85` | Calidad JPEG (0.0 - 1.0). 0.85 = 85% |
| `app.image.max-width` | `1920` | Ancho máximo antes de redimensionar |
| `app.image.max-height` | `1920` | Alto máximo antes de redimensionar |
| `app.image.thumbnail-size` | `200` | Tamaño del thumbnail (cuadrado) |
| `app.image.max-size-mb` | `10` | Tamaño máximo de archivo antes de compresión |

### Proceso de Compresión

1. **Validación**: Verifica que sea imagen válida y < 10MB
2. **Redimensionamiento**: Si excede 1920x1920, se redimensiona manteniendo aspecto
3. **Compresión JPEG**: Reduce calidad a 85% (configurable)
4. **Thumbnail**: Genera versión 200x200 cuadrada
5. **Subida**: Sube ambas versiones a S3

### Ejemplo de Reducción de Tamaño

| Original | Comprimida | Thumbnail | Ahorro |
|----------|------------|-----------|--------|
| 8.5 MB (4032x3024) | 1.2 MB (1920x1440) | 45 KB (200x200) | ~86% |
| 3.2 MB (2560x1440) | 450 KB (1920x1080) | 38 KB (200x200) | ~86% |

---

## 🔒 Seguridad y Mejores Prácticas

### ✅ Recomendaciones de Seguridad

1. **No commits con credenciales reales**
   - Usa variables de entorno
   - Agrega `application.properties` a `.gitignore` si contiene secretos

2. **Política IAM de mínimos privilegios**
   - Solo permisos S3 necesarios (PutObject, GetObject, DeleteObject)
   - Restringir a tu bucket específico

3. **HTTPS siempre**
   - AWS S3 sirve por HTTPS por defecto
   - Asegúrate que tu backend también use HTTPS en producción

4. **Validación de imágenes**
   - El backend ya valida tipo MIME
   - Considera agregar escaneo de malware para producción

5. **Límites de tamaño**
   - Default 10MB por imagen
   - Ajusta según tus necesidades en `application.properties`

### ❌ NO HACER

- ❌ No subir credenciales a Git/GitHub
- ❌ No usar credenciales de root/admin
- ❌ No hacer el bucket público sin control
- ❌ No permitir subidas ilimitadas sin autenticación

---

## 🧪 Testing

### Probar con cURL

```bash
# Subir imagen de prueba para mascota ID 1
curl -X POST http://localhost:8080/api/images/pet/1/profile \
  -F "file=@/ruta/a/tu/imagen.jpg"

# Verificar que existe
curl "http://localhost:8080/api/images/exists?url=URL_DEVUELTA_ARRIBA"
```

### Probar con Postman

1. Crear request POST a `http://localhost:8080/api/images/pet/1/profile`
2. En Body, seleccionar `form-data`
3. Agregar key `file` tipo `File`
4. Seleccionar imagen
5. Send

---

## 📊 Resumen de Archivos Modificados/Creados

### Archivos Nuevos

```
├── config/
│   └── S3Config.java ........................ Configuración AWS S3 client
├── service/
│   ├── ImageCompressionService.java ......... Compresión y thumbnails
│   └── S3Service.java ....................... Operaciones S3
├── controller/
│   └── ImageUploadController.java ........... API REST para imágenes
└── S3_IMAGE_STORAGE_DOCUMENTATION.md ........ Este documento
```

### Archivos Modificados

```
├── pom.xml .................................. Dependencias AWS SDK + imgscalr
├── application.properties ................... Configuración S3 (CAMBIAR!)
├── models/Pet.java .......................... Campos profileImageUrl/thumbnailUrl
└── models/VaccinationRecord.java ............ Campos batchImageUrl/thumbnailUrl/batchNumber
```

---

## 🎉 Checklist de Configuración

Antes de usar en producción, verifica:

- [ ] Bucket S3 creado en AWS
- [ ] Usuario IAM creado con permisos apropiados
- [ ] Credenciales AWS configuradas en `application.properties` o variables de entorno
- [ ] Política CORS configurada en el bucket (si es necesario)
- [ ] Bucket name actualizado en `application.properties`
- [ ] Región correcta configurada
- [ ] Testing realizado con imágenes de muestra
- [ ] Backup/versioning habilitado en S3 (opcional pero recomendado)

---

## 🆘 Troubleshooting

### Error: "Access Denied" al subir imagen

**Causa**: Credenciales incorrectas o permisos IAM insuficientes

**Solución**:
- Verifica que `aws.s3.access-key` y `aws.s3.secret-key` sean correctos
- Verifica que el usuario IAM tenga permiso `s3:PutObject` en el bucket

### Error: "Bucket does not exist"

**Causa**: Nombre de bucket incorrecto o bucket en otra región

**Solución**:
- Verifica `aws.s3.bucket-name` en `application.properties`
- Verifica que la región (`aws.s3.region`) coincida con la del bucket

### Error: "File is not a valid image"

**Causa**: Archivo subido no es una imagen válida

**Solución**:
- Asegúrate de subir JPG, PNG o GIF
- Verifica que el archivo no esté corrupto

### Error: "File size exceeds maximum"

**Causa**: Imagen mayor a 10MB

**Solución**:
- Comprime la imagen antes de subirla
- O aumenta `app.image.max-size-mb` en `application.properties`

---

## 📞 Próximos Pasos Sugeridos

1. ✅ Configurar credenciales reales de AWS
2. ✅ Probar subida/eliminación de imágenes
3. ⚡ Implementar autenticación en endpoints (Firebase Auth)
4. ⚡ Agregar rate limiting para prevenir abuso
5. ⚡ Implementar CDN (CloudFront) delante de S3 para mejor performance
6. ⚡ Agregar watermark automático en imágenes (opcional)
7. ⚡ Implementar presigned URLs para acceso temporal

---

**¡Sistema de almacenamiento de imágenes S3 listo para usar!** 🚀

Recuerda: **CAMBIAR las credenciales mock por las reales antes de deployment en producción.**
