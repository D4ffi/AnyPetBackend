# 💰 Deploy Económico para Pruebas - Menos de $3/mes

## 🎯 Objetivo
Deployar AnyPetBackend usando **solo free tier de AWS** o alternativas que cuesten menos de $3/mes.

---

## 🏆 Recomendaciones por Presupuesto

### Opción 1: AWS Free Tier (100% GRATIS primer año) ⭐ RECOMENDADO

| Componente | Servicio | Free Tier | Después Free Tier |
|------------|----------|-----------|-------------------|
| **Servidor** | EC2 t2.micro | 750 hrs/mes gratis | ~$8/mes |
| **Base de datos** | RDS db.t2.micro | 750 hrs/mes gratis | ~$15/mes |
| **Almacenamiento S3** | S3 Standard | 5 GB gratis | ~$0.50/mes |
| **Transferencia** | Data transfer | 15 GB/mes gratis | Variable |

**Costo total**:
- **Primer año**: $0/mes ✅
- **Después del año 1**: $23-25/mes

### Opción 2: Railway.app (Más simple, gratis al inicio) ⭐⭐

- **Costo**: $0/mes (plan hobby) + $5/mes después de créditos
- **Ventajas**: Súper simple, PostgreSQL incluido, deploy desde Git
- **Desventajas**: No es AWS, menos control

### Opción 3: Render.com (Alternativa gratuita)

- **Costo**: $0/mes (plan free)
- **Ventajas**: Deploy automático desde GitHub, PostgreSQL gratis
- **Desventajas**: Servidor "duerme" después de 15 min sin uso

### Opción 4: Fly.io (Buena alternativa)

- **Costo**: ~$0-3/mes con recursos mínimos
- **Ventajas**: PostgreSQL incluido, buen performance
- **Desventajas**: Requiere Docker

---

## ✅ RECOMENDACIÓN PARA PRUEBAS: Railway.app

Para versión de prueba, **Railway.app** es la mejor opción:

### Por qué Railway:

1. ✅ **$5 de crédito gratis** cada mes (plan Hobby)
2. ✅ **Deploy en 2 minutos** desde GitHub
3. ✅ **PostgreSQL incluido** (gratis)
4. ✅ **No requiere tarjeta** de crédito al inicio
5. ✅ **Variables de entorno** fáciles de configurar
6. ✅ **Logs en tiempo real**
7. ✅ **No "duerme"** como Render

### Limitación:
- S3 sigue siendo de AWS (necesitas configurarlo)
- O usa almacenamiento local temporalmente para pruebas

---

## 🚀 Deploy en Railway (5 minutos)

### Paso 1: Preparar el Proyecto

**1.1 Crear archivo `Procfile` (opcional pero recomendado)**

```bash
# En la raíz del proyecto
echo "web: java -Dserver.port=\$PORT -jar target/AnyPetBackend-0.0.1-SNAPSHOT.jar" > Procfile
```

**1.2 Agregar `railway.json` (configuración)**

```json
{
  "$schema": "https://railway.app/railway.schema.json",
  "build": {
    "builder": "nixpacks",
    "buildCommand": "./mvnw clean package -DskipTests"
  },
  "deploy": {
    "startCommand": "java -Dserver.port=$PORT -jar target/AnyPetBackend-0.0.1-SNAPSHOT.jar",
    "restartPolicyType": "ON_FAILURE",
    "restartPolicyMaxRetries": 10
  }
}
```

**1.3 Actualizar `application.properties` para Railway**

```properties
# Usar el puerto que Railway asigna
server.port=${PORT:8080}

# Base de datos (Railway provee estas variables automáticamente)
spring.datasource.url=${DATABASE_URL:jdbc:h2:mem:testdb}
spring.datasource.driver-class-name=${DB_DRIVER:org.h2.Driver}
spring.jpa.database-platform=${DB_DIALECT:org.hibernate.dialect.H2Dialect}
spring.jpa.hibernate.ddl-auto=update
```

### Paso 2: Subir a GitHub

```bash
# Si no has inicializado Git
git init
git add .
git commit -m "Prepare for Railway deployment"

# Crear repo en GitHub y push
git remote add origin https://github.com/tu-usuario/AnyPetBackend.git
git push -u origin main
```

### Paso 3: Deploy en Railway

1. **Ir a [railway.app](https://railway.app)**
2. **Sign up** con GitHub (gratis)
3. **New Project** → **Deploy from GitHub repo**
4. **Seleccionar** tu repositorio `AnyPetBackend`
5. Railway detecta automáticamente que es Java/Maven
6. **Deploy** automáticamente

### Paso 4: Agregar PostgreSQL

1. En tu proyecto Railway, click **+ New**
2. Seleccionar **Database** → **PostgreSQL**
3. Railway automáticamente conecta la BD con tu app
4. Variables creadas automáticamente:
   - `DATABASE_URL`
   - `POSTGRES_USER`
   - `POSTGRES_PASSWORD`
   - `POSTGRES_DB`

### Paso 5: Configurar Variables de Entorno

1. Click en tu servicio → **Variables**
2. Agregar:

```bash
SPRING_PROFILES_ACTIVE=prod
AWS_S3_ACCESS_KEY=tu_access_key
AWS_S3_SECRET_KEY=tu_secret_key
AWS_S3_BUCKET_NAME=anypet-images-bucket
AWS_S3_REGION=us-east-1
```

### Paso 6: Ver tu API

```bash
# Railway te da una URL automática
https://anypetbackend-production.up.railway.app

# Probar
curl https://tu-url.up.railway.app/api/vaccines
```

---

## 📊 Comparación Detallada: AWS vs Railway vs Render

| Característica | AWS Free Tier | Railway | Render Free |
|----------------|---------------|---------|-------------|
| **Costo (pruebas)** | $0 (1 año) | $0-3/mes | $0 |
| **Costo (después)** | $23-25/mes | $5-10/mes | $0 (limitado) |
| **Complejidad** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Deploy** | Manual CLI | Auto desde Git | Auto desde Git |
| **Base de datos** | RDS (separado) | Incluida ✅ | Incluida ✅ |
| **Logs** | CloudWatch | Dashboard ✅ | Dashboard ✅ |
| **Escalabilidad** | Alta | Media | Baja (free) |
| **Sleep/Idle** | No | No ✅ | Sí ⚠️ (15 min) |
| **Custom domain** | Sí | Sí | Sí |
| **HTTPS** | Manual | Auto ✅ | Auto ✅ |

---

## 💡 Configuración Mínima para AWS Free Tier

Si prefieres quedarte en AWS con **$0/mes**:

### Opción: AWS Lightsail (Alternativa más simple que EC2)

**Costo**: $3.50/mes (primera instancia) - Supera tu presupuesto pero es la más barata en AWS

```bash
# Crear instancia Lightsail
aws lightsail create-instances \
  --instance-names anypet-backend \
  --availability-zone us-east-1a \
  --blueprint-id amazon_linux_2 \
  --bundle-id nano_2_0

# SSH
ssh -i key.pem ec2-user@tu-ip

# Instalar Java
sudo yum install java-17-amazon-corretto

# Subir JAR
scp target/AnyPetBackend-0.0.1-SNAPSHOT.jar ec2-user@tu-ip:~/

# Correr
java -jar AnyPetBackend-0.0.1-SNAPSHOT.jar
```

### Para VERDADERO Free Tier ($0/mes):

**Elastic Beanstalk con instancia t2.micro**:

```bash
# Crear con instancia micro (gratis)
eb create anypet-test \
  --instance-type t2.micro \
  --single \
  --database.engine postgres \
  --database.instance db.t2.micro

# IMPORTANTE: Esto es gratis SOLO el primer año
```

---

## 🎯 Mi Recomendación Final para Pruebas

### Para < $3/mes: **Railway.app** 🏆

**Setup completo en 10 minutos**:

```bash
# 1. Crear railway.json
echo '{
  "build": {
    "buildCommand": "./mvnw clean package -DskipTests"
  },
  "deploy": {
    "startCommand": "java -Dserver.port=$PORT -jar target/AnyPetBackend-0.0.1-SNAPSHOT.jar"
  }
}' > railway.json

# 2. Commit y push a GitHub
git add .
git commit -m "Add Railway config"
git push

# 3. Ir a railway.app y conectar repo
# 4. Listo!
```

### Configuración S3 (mantener en AWS)

- **Opción 1**: Usar S3 de AWS (5GB gratis)
- **Opción 2**: Para pruebas, usar almacenamiento local temporalmente
- **Opción 3**: Usar Cloudinary (gratis, 10GB) para imágenes

---

## 🔄 Migración Railway → AWS (cuando crezcas)

Cuando necesites más recursos:

```bash
# 1. Exportar base de datos de Railway
railway run pg_dump > backup.sql

# 2. Importar a RDS
psql -h rds-endpoint.amazonaws.com -U postgres anypet < backup.sql

# 3. Deploy a Elastic Beanstalk
eb init
eb create production
```

---

## 💰 Resumen de Costos Reales

### Railway (RECOMENDADO para tu caso)

| Recurso | Costo |
|---------|-------|
| **Servidor** (512MB RAM) | Incluido en $5 crédito mensual |
| **PostgreSQL** (1GB) | Incluido |
| **Bandwidth** (100GB) | Incluido |
| **Total** | **$0-2/mes** con créditos ✅ |

### AWS Free Tier (Primer año)

| Recurso | Costo |
|---------|-------|
| **EC2 t2.micro** | $0 (primer año) |
| **RDS db.t2.micro** | $0 (primer año) |
| **S3** (< 5GB) | $0 |
| **Total** | **$0/mes** ✅ |

**Después del año**: ~$23/mes ⚠️

### Render.com (Gratis pero limitado)

| Recurso | Costo |
|---------|-------|
| **Servidor** (512MB) | $0 (con sleep) |
| **PostgreSQL** (1GB) | $0 |
| **Total** | **$0/mes** ✅ |

**Limitación**: Servidor "duerme" tras 15 min inactividad

---

## 🚀 Guía Rápida: Deploy en Railway (Paso a Paso)

### 1. Crear cuenta

```
https://railway.app
→ Sign up with GitHub
```

### 2. Preparar archivos

**railway.json**:
```json
{
  "build": {
    "buildCommand": "./mvnw clean package -DskipTests"
  },
  "deploy": {
    "startCommand": "java -Dserver.port=$PORT -jar target/AnyPetBackend-0.0.1-SNAPSHOT.jar"
  }
}
```

**nixpacks.toml** (alternativa):
```toml
[phases.build]
cmds = ["./mvnw clean package -DskipTests"]

[start]
cmd = "java -Dserver.port=$PORT -jar target/AnyPetBackend-0.0.1-SNAPSHOT.jar"
```

### 3. Push a GitHub

```bash
git add railway.json
git commit -m "Add Railway config"
git push
```

### 4. Deploy

1. Railway → **New Project**
2. **Deploy from GitHub repo**
3. Seleccionar repo
4. **Add PostgreSQL** (click +New → Database → PostgreSQL)
5. **Add variables**:
   - `AWS_S3_ACCESS_KEY`
   - `AWS_S3_SECRET_KEY`
   - `AWS_S3_BUCKET_NAME`
   - `AWS_S3_REGION`

### 5. ¡Listo!

```
Tu API: https://anypetbackend-production.up.railway.app
```

---

## 🎯 Checklist de Deploy Económico

- [ ] Cuenta Railway creada (gratis)
- [ ] Repositorio en GitHub
- [ ] `railway.json` creado
- [ ] `application.properties` actualizado con `${PORT}`
- [ ] Código pusheado a GitHub
- [ ] Proyecto creado en Railway
- [ ] PostgreSQL agregada
- [ ] Variables de entorno configuradas
- [ ] S3 bucket creado en AWS (free tier)
- [ ] Testing de endpoints

---

## 📞 Alternativas Gratuitas para S3

Si quieres evitar AWS S3:

### Cloudinary (RECOMENDADO para pruebas)

- **Gratis**: 25 créditos/mes = ~10GB almacenamiento + 25GB bandwidth
- **Ventajas**: API super simple, transformaciones automáticas
- **Java SDK**: Disponible

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.cloudinary</groupId>
    <artifactId>cloudinary-http44</artifactId>
    <version>1.33.0</version>
</dependency>
```

### Backblaze B2 (Súper barato)

- **Gratis**: 10GB almacenamiento + 1GB descarga/día
- **Después**: $0.005/GB (10x más barato que S3)
- **S3-compatible**: Usa mismo SDK

---

## 🎉 Resultado Final

Con Railway + AWS S3 Free Tier:

✅ **Costo**: $0-2/mes
✅ **Setup**: 10 minutos
✅ **Deploy**: Automático desde Git
✅ **PostgreSQL**: Incluida
✅ **Escalable**: Fácil upgrade cuando crezcas

**Tu API estará en**: `https://tu-proyecto.up.railway.app`

---

**¿Listo para deployar?** 🚀

```bash
# 3 comandos y listo:
echo '{"build":{"buildCommand":"./mvnw clean package -DskipTests"},"deploy":{"startCommand":"java -Dserver.port=$PORT -jar target/AnyPetBackend-0.0.1-SNAPSHOT.jar"}}' > railway.json
git add . && git commit -m "Deploy to Railway" && git push

# Luego en railway.app:
# New Project → Deploy from GitHub → Select repo → Done!
```
