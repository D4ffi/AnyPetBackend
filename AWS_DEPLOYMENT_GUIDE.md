# 🚀 Guía de Deploy a AWS Elastic Beanstalk - AnyPetBackend

## 📋 Índice
- [Por qué Elastic Beanstalk](#por-qué-elastic-beanstalk)
- [Requisitos Previos](#requisitos-previos)
- [Configuración Inicial](#configuración-inicial)
- [Deploy Paso a Paso](#deploy-paso-a-paso)
- [Configurar Base de Datos RDS](#configurar-base-de-datos-rds)
- [Variables de Entorno](#variables-de-entorno)
- [Actualizar la Aplicación](#actualizar-la-aplicación)
- [Monitoreo y Logs](#monitoreo-y-logs)
- [Troubleshooting](#troubleshooting)

---

## 🎯 Por qué Elastic Beanstalk

**AWS Elastic Beanstalk** es un servicio PaaS (Platform as a Service) que:

- ✅ Despliega tu Spring Boot JAR en minutos
- ✅ Maneja autoescalado automáticamente
- ✅ Actualiza con un solo comando
- ✅ Integración sencilla con RDS PostgreSQL
- ✅ HTTPS automático con certificados SSL gratuitos
- ✅ Load balancer incluido
- ✅ Monitoreo con CloudWatch
- ✅ Rollback instantáneo a versiones anteriores

### Alternativas Consideradas

| Opción | Ventajas | Desventajas | Veredicto |
|--------|----------|-------------|-----------|
| **Elastic Beanstalk** | Muy simple, maneja todo | Menos control granular | ⭐ **RECOMENDADO** |
| **App Runner** | Moderno, simple | Más caro, menos maduro | ⭐ Buena alternativa |
| **EC2 Manual** | Control total | Mucho trabajo manual | ❌ No recomendado |
| **ECS/Fargate** | Escalabilidad máxima | Curva de aprendizaje | ⚠️ Solo si ya usas Docker |
| **Lambda** | Serverless, barato | Complejo para Spring Boot | ❌ No ideal |

---

## ✅ Requisitos Previos

### 1. Cuenta AWS
- Crear cuenta en [aws.amazon.com](https://aws.amazon.com)
- Activar **Free Tier** (750 horas/mes gratis primer año)

### 2. AWS CLI
```bash
# macOS
brew install awscli

# Linux
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install

# Windows
# Descargar desde: https://aws.amazon.com/cli/

# Verificar instalación
aws --version
```

### 3. EB CLI (Elastic Beanstalk CLI)
```bash
# macOS/Linux
pip install awsebcli --upgrade --user

# Windows
pip install awsebcli

# Verificar instalación
eb --version
```

### 4. Configurar Credenciales AWS
```bash
aws configure
```

Te pedirá:
- **AWS Access Key ID**: Obtener desde IAM Console
- **AWS Secret Access Key**: Obtener desde IAM Console
- **Default region**: `us-east-1` (o tu región preferida)
- **Default output format**: `json`

---

## 🔧 Configuración Inicial

### Paso 1: Preparar el Proyecto

**1.1 Actualizar `application.properties` para Producción**

Crea un perfil de producción: `src/main/resources/application-prod.properties`

```properties
spring.application.name=AnyPetBackend

# ========================================
# PostgreSQL Database (RDS Production)
# ========================================
spring.datasource.url=jdbc:postgresql://${RDS_HOSTNAME:localhost}:${RDS_PORT:5432}/${RDS_DB_NAME:anypet}
spring.datasource.username=${RDS_USERNAME:postgres}
spring.datasource.password=${RDS_PASSWORD:}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update

# ========================================
# AWS S3 Configuration (Production)
# ========================================
aws.s3.access-key=${AWS_S3_ACCESS_KEY}
aws.s3.secret-key=${AWS_S3_SECRET_KEY}
aws.s3.region=${AWS_S3_REGION:us-east-1}
aws.s3.bucket-name=${AWS_S3_BUCKET_NAME}

# Image upload settings
app.image.max-size-mb=10
app.image.compression-quality=0.85
app.image.max-width=1920
app.image.max-height=1920
app.image.thumbnail-size=200

# ========================================
# Server Configuration
# ========================================
server.port=5000
server.compression.enabled=true
server.compression.mime-types=application/json,application/xml,text/html,text/xml,text/plain
```

**1.2 Construir el JAR**

```bash
./mvnw clean package -DskipTests
```

Esto genera: `target/AnyPetBackend-0.0.1-SNAPSHOT.jar`

### Paso 2: Inicializar Elastic Beanstalk

```bash
# Desde la raíz del proyecto
eb init

# Responde las preguntas:
# - Select a default region: us-east-1 (o tu región)
# - Application name: anypet-backend
# - Platform: Java
# - Platform version: Corretto 17 (Java 17)
# - Do you want to set up SSH? Yes (recomendado)
```

Esto crea `.elasticbeanstalk/config.yml`

---

## 🚀 Deploy Paso a Paso

### Paso 3: Crear Entorno de Elastic Beanstalk

```bash
# Crear entorno de producción
eb create anypet-production \
  --instance-type t3.small \
  --platform "Corretto 17" \
  --single

# Opciones:
# --single: Una sola instancia (gratis, bueno para empezar)
# --instance-type t3.small: Tipo de instancia (puedes usar t2.micro para free tier)
```

Este comando:
- Crea la infraestructura (EC2, Security Groups, Load Balancer)
- Despliega tu JAR automáticamente
- Tarda ~5-10 minutos

### Paso 4: Configurar Variables de Entorno

```bash
# Configurar perfil de Spring
eb setenv SPRING_PROFILES_ACTIVE=prod

# AWS S3 (usa tus valores reales)
eb setenv AWS_S3_ACCESS_KEY=tu_access_key_aqui
eb setenv AWS_S3_SECRET_KEY=tu_secret_key_aqui
eb setenv AWS_S3_REGION=us-east-1
eb setenv AWS_S3_BUCKET_NAME=anypet-images-production

# Nota: La base de datos RDS se configura en el siguiente paso
```

---

## 🗄️ Configurar Base de Datos RDS

### Opción 1: Crear RDS desde EB Console (Recomendado)

1. Ir a [AWS Elastic Beanstalk Console](https://console.aws.amazon.com/elasticbeanstalk)
2. Seleccionar tu aplicación `anypet-backend`
3. Ir a **Configuration** → **Database**
4. Click **Edit**
5. Configurar:
   - **Engine**: postgres
   - **Version**: PostgreSQL 15
   - **Instance class**: db.t3.micro (free tier)
   - **Storage**: 20 GB
   - **Username**: postgres
   - **Password**: (tu contraseña segura)
   - **Retention**: Delete (para desarrollo)
6. **Apply**

Esto automáticamente configura las variables:
- `RDS_HOSTNAME`
- `RDS_PORT`
- `RDS_DB_NAME`
- `RDS_USERNAME`
- `RDS_PASSWORD`

### Opción 2: Crear RDS Manualmente (Producción)

```bash
# Para producción, crear RDS separado (no se borra al eliminar EB)
aws rds create-db-instance \
  --db-instance-identifier anypet-postgres \
  --db-instance-class db.t3.micro \
  --engine postgres \
  --master-username postgres \
  --master-user-password TU_PASSWORD_SEGURA_AQUI \
  --allocated-storage 20 \
  --publicly-accessible

# Luego configurar variables en EB
eb setenv RDS_HOSTNAME=endpoint-de-tu-rds.region.rds.amazonaws.com
eb setenv RDS_PORT=5432
eb setenv RDS_DB_NAME=anypet
eb setenv RDS_USERNAME=postgres
eb setenv RDS_PASSWORD=TU_PASSWORD_SEGURA_AQUI
```

---

## 🔄 Actualizar la Aplicación

### Método 1: Deploy desde CLI (Más Rápido)

```bash
# 1. Hacer cambios en tu código
# 2. Construir nuevo JAR
./mvnw clean package -DskipTests

# 3. Deploy
eb deploy

# Listo! En ~2-3 minutos tu nueva versión está live
```

### Método 2: Deploy con Git

```bash
# EB puede deployar desde Git automáticamente
git add .
git commit -m "Update API"

# Deploy desde commit actual
eb deploy --staged
```

### Verificar Deploy

```bash
# Ver estado
eb status

# Ver logs en tiempo real
eb logs --stream

# Abrir la aplicación en el navegador
eb open
```

---

## 📊 Variables de Entorno

### Ver Variables Actuales

```bash
eb printenv
```

### Configurar Múltiples Variables

Crear archivo `.ebextensions/environment.config`:

```yaml
option_settings:
  aws:elasticbeanstalk:application:environment:
    SPRING_PROFILES_ACTIVE: prod
    AWS_S3_REGION: us-east-1
    # No pongas secretos aquí, usa eb setenv
```

### Variables Críticas a Configurar

```bash
# Spring Profile
eb setenv SPRING_PROFILES_ACTIVE=prod

# AWS S3
eb setenv AWS_S3_ACCESS_KEY=AKIAIOSFODNN7EXAMPLE
eb setenv AWS_S3_SECRET_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
eb setenv AWS_S3_BUCKET_NAME=anypet-images-production
eb setenv AWS_S3_REGION=us-east-1

# Firebase (si usas archivo de credenciales)
# Opción: Subir archivo a S3 y referenciar la ruta
eb setenv GOOGLE_APPLICATION_CREDENTIALS=/var/app/current/.secrets/firebase-credentials.json

# Base de datos (si es RDS externo)
eb setenv RDS_HOSTNAME=your-rds-endpoint.rds.amazonaws.com
eb setenv RDS_PORT=5432
eb setenv RDS_DB_NAME=anypet
eb setenv RDS_USERNAME=postgres
eb setenv RDS_PASSWORD=your-secure-password
```

---

## 📈 Monitoreo y Logs

### Ver Logs

```bash
# Logs de la última hora
eb logs

# Logs en tiempo real (streaming)
eb logs --stream

# Descargar logs completos
eb logs --all
```

### Monitoreo con CloudWatch

1. Ir a [CloudWatch Console](https://console.aws.amazon.com/cloudwatch)
2. Ver métricas de tu aplicación:
   - CPU utilization
   - Network traffic
   - Request count
   - Latency
   - Errores 4xx/5xx

### Health Monitoring

```bash
# Ver salud del entorno
eb health

# Dashboard web
eb console
```

---

## 🔒 HTTPS y Dominio Personalizado

### Configurar HTTPS

1. **Obtener Certificado SSL Gratuito**:
   - Ir a [AWS Certificate Manager](https://console.aws.amazon.com/acm)
   - Request public certificate
   - Dominio: `api.tudominio.com`
   - Validación DNS o Email

2. **Configurar Load Balancer**:
   ```bash
   # En EB Console → Configuration → Load Balancer
   # Agregar listener HTTPS en puerto 443
   # Seleccionar certificado SSL
   ```

### Configurar Dominio

1. En Route 53 o tu DNS provider:
   ```
   api.tudominio.com  CNAME  anypet-production.us-east-1.elasticbeanstalk.com
   ```

---

## 🛠️ Troubleshooting

### Error: "Application deployment failed"

```bash
# Ver logs detallados
eb logs

# Revisar errores comunes:
# - Puerto incorrecto (debe ser 5000)
# - Variables de entorno faltantes
# - Base de datos no accesible
```

### Error: "Health status is Severe"

```bash
# Ver qué está fallando
eb health --refresh

# Verificar logs
eb ssh  # conectar por SSH
tail -f /var/log/eb-engine.log
```

### Error: "Cannot connect to RDS"

```bash
# Verificar security groups
# EB debe tener acceso al security group de RDS

# En RDS Console:
# - Security Group debe permitir tráfico desde EB security group
# - Puerto 5432 abierto
```

### Reiniciar Aplicación

```bash
eb restart
```

### Rollback a Versión Anterior

```bash
# Ver versiones disponibles
eb appversion

# Hacer rollback
eb deploy --version "version-label"
```

---

## 💰 Costos Estimados

### Free Tier (Primer Año)

- **EC2 t2.micro**: 750 horas/mes gratis
- **RDS db.t2.micro**: 750 horas/mes gratis
- **S3**: 5 GB gratis
- **Data Transfer**: 15 GB/mes gratis

**Costo total**: $0 - $5/mes (si te mantienes en free tier)

### Después del Free Tier

- **EC2 t3.small**: ~$15/mes
- **RDS db.t3.micro**: ~$15/mes
- **S3**: ~$1-5/mes (dependiendo uso)
- **Load Balancer**: ~$18/mes
- **Data Transfer**: Variable

**Costo total estimado**: $50-70/mes para tráfico moderado

### Optimizar Costos

```bash
# Usar instancia más pequeña
eb scale 1 --instance-type t2.micro

# Apagar en horas no pico (desarrollo)
eb terminate  # cuando no uses
eb create     # cuando necesites
```

---

## 🎯 Comandos Útiles (Cheat Sheet)

```bash
# Inicializar proyecto
eb init

# Crear entorno
eb create nombre-entorno

# Deploy
eb deploy

# Ver estado
eb status

# Ver logs
eb logs
eb logs --stream

# Variables de entorno
eb setenv KEY=value
eb printenv

# Abrir en navegador
eb open

# SSH al servidor
eb ssh

# Escalar (cambiar número de instancias)
eb scale 2

# Ver salud
eb health

# Console web
eb console

# Terminar entorno (CUIDADO: borra todo)
eb terminate
```

---

## 📝 Checklist de Deploy

Antes de hacer deploy a producción:

- [ ] JAR construido correctamente (`./mvnw clean package`)
- [ ] `application-prod.properties` configurado
- [ ] Bucket S3 creado y configurado
- [ ] Variables de entorno configuradas en EB
- [ ] RDS PostgreSQL creado y accesible
- [ ] Security groups configurados
- [ ] Credenciales Firebase subidas (si aplica)
- [ ] CORS configurado en S3
- [ ] Testing local exitoso
- [ ] Backup de base de datos (si hay datos existentes)
- [ ] Certificado SSL solicitado (para HTTPS)
- [ ] Dominio configurado en DNS

---

## 🚀 Flujo de Trabajo Completo

### Primera Vez (Setup)

```bash
# 1. Instalar herramientas
brew install awscli
pip install awsebcli

# 2. Configurar AWS
aws configure

# 3. Construir JAR
./mvnw clean package

# 4. Inicializar EB
eb init

# 5. Crear entorno
eb create anypet-production

# 6. Configurar variables
eb setenv SPRING_PROFILES_ACTIVE=prod
eb setenv AWS_S3_ACCESS_KEY=...
eb setenv AWS_S3_SECRET_KEY=...
eb setenv AWS_S3_BUCKET_NAME=...

# 7. Configurar RDS desde console
# (Ver sección "Configurar Base de Datos RDS")

# 8. Verificar
eb open
```

### Actualizaciones Posteriores

```bash
# 1. Hacer cambios en código
# 2. Construir
./mvnw clean package -DskipTests

# 3. Deploy
eb deploy

# 4. Verificar
eb logs --stream
eb open
```

---

## 🎉 Resultado Final

Tu API estará disponible en:
```
http://anypet-production.us-east-1.elasticbeanstalk.com
```

Con endpoints como:
```
POST http://tu-url/api/images/pet/123/profile
GET  http://tu-url/api/vaccines
GET  http://tu-url/api/vaccines/pet-type/DOG
```

---

## 🆘 Soporte

- [Documentación Elastic Beanstalk](https://docs.aws.amazon.com/elasticbeanstalk/)
- [EB CLI Reference](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/eb-cli3.html)
- [AWS Free Tier](https://aws.amazon.com/free/)

---

**¡Listo para deployar en AWS con Elastic Beanstalk!** 🚀

El proceso completo toma ~30 minutos la primera vez, y actualizaciones futuras son solo:
```bash
./mvnw clean package && eb deploy
```
