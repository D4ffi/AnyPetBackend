# AWS IAM Role Setup for Elastic Beanstalk

Esta guía explica cómo configurar IAM Roles para que tu aplicación en AWS Elastic Beanstalk pueda acceder a S3 sin necesidad de credenciales explícitas.

## ¿Por qué usar IAM Roles?

✅ **Ventajas:**
- No necesitas manejar access keys ni secret keys
- Las credenciales se rotan automáticamente
- Más seguro (no hay riesgo de exponer credenciales en el código)
- Mejor práctica recomendada por AWS
- No requiere variables de entorno con credenciales

❌ **Desventajas de usar access keys:**
- Riesgo de exposición en repositorios
- Necesitas rotarlas manualmente
- Pueden expirar y romper la aplicación

## Paso 1: Crear una política de permisos S3

1. Ve a **AWS Console → IAM → Policies**
2. Haz clic en **"Create Policy"**
3. Selecciona la pestaña **JSON** y pega lo siguiente:

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
                "s3:ListBucket",
                "s3:GetObjectMetadata",
                "s3:HeadObject"
            ],
            "Resource": [
                "arn:aws:s3:::anypet-images-bucket",
                "arn:aws:s3:::anypet-images-bucket/*"
            ]
        }
    ]
}
```

4. Reemplaza `anypet-images-bucket` con el nombre real de tu bucket S3
5. Haz clic en **"Next"**
6. Nombre de la política: `AnyPet-S3-Access-Policy`
7. Haz clic en **"Create Policy"**

## Paso 2: Crear o modificar el IAM Role para EC2

### Opción A: Crear un nuevo IAM Role

1. Ve a **AWS Console → IAM → Roles**
2. Haz clic en **"Create Role"**
3. Tipo de entidad de confianza: **"AWS service"**
4. Caso de uso: **"EC2"**
5. Haz clic en **"Next"**
6. Busca y selecciona estas políticas:
   - `AnyPet-S3-Access-Policy` (la que creaste en el paso 1)
   - `AWSElasticBeanstalkWebTier` (política predefinida)
   - `AWSElasticBeanstalkWorkerTier` (política predefinida, si usas workers)
7. Haz clic en **"Next"**
8. Nombre del rol: `aws-elasticbeanstalk-ec2-role-anypet`
9. Haz clic en **"Create Role"**

### Opción B: Modificar un IAM Role existente

Si ya tienes un IAM Role para Elastic Beanstalk:

1. Ve a **AWS Console → IAM → Roles**
2. Busca el rol de tu instancia Beanstalk (usualmente `aws-elasticbeanstalk-ec2-role`)
3. En la pestaña **"Permissions"**, haz clic en **"Add permissions" → "Attach policies"**
4. Busca y selecciona `AnyPet-S3-Access-Policy`
5. Haz clic en **"Add permissions"**

## Paso 3: Asignar el IAM Role a tu aplicación Beanstalk

### Para una aplicación nueva:

1. Al crear tu aplicación en Elastic Beanstalk
2. En la sección **"Service Access"**:
   - **Service role**: Selecciona o crea un service role
   - **EC2 instance profile**: Selecciona `aws-elasticbeanstalk-ec2-role-anypet`

### Para una aplicación existente:

1. Ve a **AWS Console → Elastic Beanstalk**
2. Selecciona tu aplicación
3. Ve a **"Configuration"**
4. En la sección **"Security"**, haz clic en **"Edit"**
5. En **"IAM instance profile"**, selecciona: `aws-elasticbeanstalk-ec2-role-anypet`
6. Haz clic en **"Apply"**
7. Espera a que el entorno se actualice (puede tomar varios minutos)

## Paso 4: Crear el bucket S3 (si no existe)

1. Ve a **AWS Console → S3**
2. Haz clic en **"Create bucket"**
3. Nombre del bucket: `anypet-images-bucket` (debe coincidir con `application.properties`)
4. Región: `us-east-1` (debe coincidir con `application.properties`)
5. Configuración de acceso:
   - **Block all public access**: ✅ Activado (recomendado)
   - La aplicación accederá usando el IAM Role, no necesita acceso público
6. Haz clic en **"Create bucket"**

### Configurar CORS (si necesitas acceso desde frontend)

Si tu frontend necesita acceder directamente a las imágenes:

1. Ve al bucket en S3
2. Ve a la pestaña **"Permissions"**
3. Scroll hasta **"Cross-origin resource sharing (CORS)"**
4. Haz clic en **"Edit"** y pega:

```json
[
    {
        "AllowedHeaders": ["*"],
        "AllowedMethods": ["GET", "HEAD"],
        "AllowedOrigins": ["*"],
        "ExposeHeaders": []
    }
]
```

5. Haz clic en **"Save changes"**

## Paso 5: Verificar la configuración

### Actualizar application.properties

Asegúrate de que tu `application.properties` tenga:

```properties
aws.s3.region=us-east-1
aws.s3.bucket-name=anypet-images-bucket
```

### Probar en desarrollo local

Para probar localmente, configura el AWS CLI:

```bash
# Opción 1: Configurar AWS CLI (recomendado)
aws configure
# Te pedirá: Access Key ID, Secret Access Key, Region

# Opción 2: Variables de entorno (temporal)
export AWS_ACCESS_KEY_ID=tu_access_key
export AWS_SECRET_ACCESS_KEY=tu_secret_key
export AWS_REGION=us-east-1
```

## Paso 6: Desplegar a Beanstalk

1. Compila tu aplicación:
```bash
./mvnw clean package -DskipTests
```

2. Sube el JAR a Beanstalk:
   - Ve a tu aplicación en Elastic Beanstalk
   - Haz clic en **"Upload and deploy"**
   - Selecciona el archivo: `target/AnyPetBackend-0.0.1-SNAPSHOT.jar`
   - Haz clic en **"Deploy"**

3. Verifica los logs:
   - Ve a **"Logs" → "Request Logs" → "Last 100 Lines"**
   - Busca errores relacionados con S3 o credenciales

## Troubleshooting

### Error: "Unable to load credentials from any of the providers"

**Causa:** El IAM Role no está asignado correctamente.

**Solución:**
1. Verifica que el IAM Role esté asignado en la configuración de Beanstalk
2. Reinicia el entorno si es necesario

### Error: "Access Denied" al subir archivos a S3

**Causa:** El IAM Role no tiene los permisos correctos.

**Solución:**
1. Verifica que la política S3 esté adjunta al rol
2. Revisa que el nombre del bucket en la política coincida con el real
3. Verifica que los ARN en la política sean correctos

### Error: "Bucket does not exist"

**Causa:** El bucket no existe o el nombre no coincide.

**Solución:**
1. Verifica que el bucket exista en S3
2. Verifica que `aws.s3.bucket-name` en `application.properties` sea correcto
3. Verifica que la región del bucket coincida con `aws.s3.region`

## Resumen de archivos modificados

- ✅ `S3Config.java` - Ahora usa `DefaultCredentialsProvider` en lugar de credenciales estáticas
- ✅ `application.properties` - Eliminadas las credenciales mock, solo región y bucket name
- ✅ `S3Service.java` - No requiere cambios, funciona automáticamente con el nuevo config

## Recursos adicionales

- [AWS IAM Roles for EC2](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/iam-roles-for-amazon-ec2.html)
- [Elastic Beanstalk IAM Roles](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/concepts-roles.html)
- [AWS SDK for Java - Credentials](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/credentials.html)
