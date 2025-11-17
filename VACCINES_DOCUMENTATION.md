# 💉 Sistema de Gestión de Vacunas - AnyPetBackend

## 📋 Índice
- [Visión General](#visión-general)
- [Arquitectura](#arquitectura)
- [Vacunas Incluidas](#vacunas-incluidas)
- [API Endpoints](#api-endpoints)
- [Ejemplos de Uso](#ejemplos-de-uso)
- [Base de Datos](#base-de-datos)

---

## 🎯 Visión General

El sistema de gestión de vacunas almacena información sobre vacunas para diferentes tipos de mascotas en la **base de datos relacional H2/PostgreSQL**. Los datos se cargan automáticamente al iniciar la aplicación.

### Tipos de Mascotas Soportados

| Tipo | Español | Inglés | DiscriminatorValue |
|------|---------|--------|-------------------|
| DOG | Perro | Dog | DOG |
| CAT | Gato | Cat | CAT |
| HAMSTER | Hamster | Hamster | HAMSTER |
| TURTLE | Tortuga | Turtle | TURTLE |
| RABBIT | Conejo | Rabbit | RABBIT |
| PARAKEET | Periquito | Parakeet | PARAKEET |
| DUCK | Pato | Duck | DUCK |

---

## 🏗️ Arquitectura

### Componentes Creados

1. **`PetType` (Enum)** - `models/PetType.java`
   - Define los tipos de mascotas soportados
   - Incluye nombres en español e inglés

2. **`Vaccine` (Entidad JPA)** - `models/Vaccine.java`
   ```java
   - Long id
   - String name              // Nombre de la vacuna
   - String targetSpecies     // Tipo de mascota (DOG, CAT, etc.)
   - String description       // Descripción detallada
   - boolean isCore           // true = esencial, false = opcional
   ```

3. **`VaccineRepository`** - `repository/VaccineRepository.java`
   - Métodos de búsqueda por especie, core status, etc.

4. **`VaccineService`** - `service/VaccineService.java`
   - Lógica de negocio para gestión de vacunas
   - Estadísticas y consultas especializadas

5. **`VaccineController`** - `controller/VaccineController.java`
   - API REST para acceso a vacunas

6. **`DataInitializer`** - `config/DataInitializer.java`
   - Carga automática de vacunas al iniciar la aplicación
   - Solo se ejecuta si la base de datos está vacía

7. **Entidades de Mascotas**:
   - `Dog.java`, `Cat.java`, `Hamster.java`, `Turtle.java`
   - `Rabbit.java`, `Parakeet.java`, `Duck.java`

---

## 💉 Vacunas Incluidas

### 🐕 Perros (Dogs) - 8 vacunas

| Vacuna | Tipo | Descripción |
|--------|------|-------------|
| Rabia | Core | Obligatoria, protege contra enfermedad mortal zoonótica |
| Moquillo Canino | Core | Virus grave que afecta múltiples sistemas |
| Parvovirus Canino | Core | Altamente contagioso, mortal en cachorros |
| Hepatitis Infecciosa | Core | Afecta hígado, riñones y otros órganos |
| Leptospirosis | Opcional | Bacteria que causa insuficiencia renal/hepática |
| Bordetella | Opcional | Tos de las perreras |
| Parainfluenza Canina | Opcional | Virus respiratorio |
| Coronavirus Canino | Opcional | Infecciones gastrointestinales |

### 🐱 Gatos (Cats) - 5 vacunas

| Vacuna | Tipo | Descripción |
|--------|------|-------------|
| Rabia | Core | Obligatoria en muchas regiones |
| FVRCP | Core | Triple felina (Herpes, Calici, Panleucopenia) |
| Leucemia Felina (FeLV) | Opcional | Para gatos que salen al exterior |
| Clamidia Felina | Opcional | Conjuntivitis y problemas respiratorios |
| Inmunodeficiencia Felina | Opcional | Similar al VIH |

### 🐰 Conejos (Rabbits) - 3 vacunas

| Vacuna | Tipo | Descripción |
|--------|------|-------------|
| Mixomatosis | Core | Transmitida por mosquitos, altamente mortal |
| RHD/VHD | Core | Enfermedad hemorrágica vírica |
| RHD2 | Core | Variante 2 de la enfermedad hemorrágica |

### 🐹 Hamsters - 1 vacuna

| Vacuna | Tipo | Descripción |
|--------|------|-------------|
| Rabia (Roedores) | Opcional | No rutinaria, requerida en ciertos casos |

### 🐢 Tortugas (Turtles) - 1 vacuna

| Vacuna | Tipo | Descripción |
|--------|------|-------------|
| Vacuna Experimental | Opcional | Consultar veterinario especializado |

### 🦜 Periquitos (Parakeets) - 3 vacunas

| Vacuna | Tipo | Descripción |
|--------|------|-------------|
| Poliomavirus Aviar | Opcional | Grave en aves jóvenes |
| Enfermedad de Newcastle | Opcional | Respiratoria, altamente contagiosa |
| Viruela Aviar | Opcional | Transmitida por mosquitos |

### 🦆 Patos (Ducks) - 5 vacunas

| Vacuna | Tipo | Descripción |
|--------|------|-------------|
| Cólera Aviar | Core | Bacteria Pasteurella multocida |
| Enfermedad de Newcastle | Core | Virus de Newcastle |
| Hepatitis Viral del Pato | Core | Mortal en patitos jóvenes |
| Influenza Aviar | Opcional | Importancia en salud pública |
| Enteritis Viral | Opcional | Plague del pato, contagiosa |

**Total: 31 vacunas** (17 Core + 14 Opcionales)

---

## 🔌 API Endpoints

### Base URL: `http://localhost:8080/api/vaccines`

| Método | Endpoint | Descripción | Ejemplo |
|--------|----------|-------------|---------|
| GET | `/` | Todas las vacunas | `GET /api/vaccines` |
| GET | `/{id}` | Vacuna por ID | `GET /api/vaccines/1` |
| GET | `/pet-type/{petType}` | Vacunas por tipo mascota | `GET /api/vaccines/pet-type/DOG` |
| GET | `/pet-type/{petType}/core` | Vacunas core por tipo | `GET /api/vaccines/pet-type/CAT/core` |
| GET | `/core` | Todas las vacunas core | `GET /api/vaccines/core` |
| GET | `/optional` | Todas las vacunas opcionales | `GET /api/vaccines/optional` |
| GET | `/statistics/{petType}` | Estadísticas por tipo | `GET /api/vaccines/statistics/RABBIT` |
| GET | `/count` | Total de vacunas | `GET /api/vaccines/count` |
| POST | `/` | Crear vacuna | `POST /api/vaccines` + body |
| PUT | `/{id}` | Actualizar vacuna | `PUT /api/vaccines/1` + body |
| DELETE | `/{id}` | Eliminar vacuna | `DELETE /api/vaccines/1` |

---

## 📝 Ejemplos de Uso

### 1️⃣ Obtener todas las vacunas para perros

```bash
curl http://localhost:8080/api/vaccines/pet-type/DOG
```

**Respuesta:**
```json
[
  {
    "id": 1,
    "name": "Rabia (Perros)",
    "targetSpecies": "DOG",
    "description": "Vacuna contra el virus de la rabia...",
    "core": true
  },
  {
    "id": 2,
    "name": "Moquillo Canino (Distemper)",
    "targetSpecies": "DOG",
    "description": "Protege contra el virus del moquillo...",
    "core": true
  }
  // ... más vacunas
]
```

### 2️⃣ Obtener solo vacunas esenciales para gatos

```bash
curl http://localhost:8080/api/vaccines/pet-type/CAT/core
```

### 3️⃣ Obtener estadísticas de vacunas para conejos

```bash
curl http://localhost:8080/api/vaccines/statistics/RABBIT
```

**Respuesta:**
```json
{
  "petType": "RABBIT",
  "totalVaccines": 3,
  "coreVaccines": 3,
  "optionalVaccines": 0
}
```

### 4️⃣ Crear una nueva vacuna personalizada

```bash
curl -X POST http://localhost:8080/api/vaccines \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Nueva Vacuna Personalizada",
    "targetSpecies": "DOG",
    "description": "Descripción de la vacuna",
    "core": false
  }'
```

### 5️⃣ Usar el servicio desde Java

```java
@Autowired
private VaccineService vaccineService;

// Obtener vacunas para perros
List<Vaccine> dogVaccines = vaccineService.getVaccinesForPetType(PetType.DOG);

// Obtener solo vacunas core para gatos
List<Vaccine> catCoreVaccines = vaccineService.getCoreVaccinesForPetType(PetType.CAT);

// Obtener estadísticas
VaccineService.VaccineStatistics stats =
    vaccineService.getStatisticsForPetType(PetType.RABBIT);
System.out.println(stats); // Tipo: RABBIT | Total: 3 | Esenciales: 3 | Opcionales: 0
```

---

## 💾 Base de Datos

### Tabla `vaccine`

```sql
CREATE TABLE vaccine (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255),
    target_species VARCHAR(50),
    description TEXT,
    is_core BOOLEAN
);
```

### Acceso a H2 Console (Desarrollo)

1. Iniciar aplicación: `./mvnw spring-boot:run`
2. Abrir navegador: `http://localhost:8080/h2-console`
3. Configuración:
   - JDBC URL: `jdbc:h2:mem:testdb`
   - Username: `sa`
   - Password: (dejar vacío)

### Consultas SQL Útiles

```sql
-- Ver todas las vacunas
SELECT * FROM vaccine;

-- Contar vacunas por especie
SELECT target_species, COUNT(*) as total
FROM vaccine
GROUP BY target_species;

-- Vacunas core para perros
SELECT * FROM vaccine
WHERE target_species = 'DOG' AND is_core = true;

-- Total de vacunas core vs opcionales
SELECT is_core, COUNT(*) as total
FROM vaccine
GROUP BY is_core;
```

---

## 🚀 Inicio Rápido

### 1. Iniciar la aplicación

```bash
./mvnw spring-boot:run
```

### 2. Verificar que las vacunas se cargaron

```bash
curl http://localhost:8080/api/vaccines/count
```

Debería devolver: `31`

### 3. Explorar vacunas por tipo

```bash
# Perros
curl http://localhost:8080/api/vaccines/pet-type/DOG | jq

# Gatos
curl http://localhost:8080/api/vaccines/pet-type/CAT | jq

# Conejos
curl http://localhost:8080/api/vaccines/pet-type/RABBIT | jq
```

---

## ❓ Preguntas Frecuentes

### ¿Por qué usar base de datos en lugar de Enums?

✅ **Ventajas de la base de datos:**
- Flexibilidad para agregar/modificar vacunas sin recompilar
- Campos descriptivos (descripción, tipo core/opcional)
- Relaciones con `VaccinationRecord` eficientes
- Consultas dinámicas por especie, tipo, etc.
- Escalable para agregar más información (fabricante, lotes, etc.)

❌ **Limitaciones de Enums:**
- Requieren recompilación para cambios
- No permiten descripciones largas
- Dificultan las búsquedas complejas
- No son adecuados para datos con relaciones

### ¿Los datos se pierden al reiniciar?

**En desarrollo (H2 in-memory):** Sí, se pierden pero se recargan automáticamente con `DataInitializer`.

**En producción (PostgreSQL):** No, los datos persisten. `DataInitializer` solo carga datos si la tabla está vacía.

### ¿Cómo agregar una nueva vacuna manualmente?

Opción 1 - Vía API:
```bash
curl -X POST http://localhost:8080/api/vaccines \
  -H "Content-Type: application/json" \
  -d '{"name": "...", "targetSpecies": "DOG", "description": "...", "core": true}'
```

Opción 2 - Editar `DataInitializer.java` y reiniciar:
```java
createVaccine(vaccineRepository,
    "Nombre Vacuna",
    PetType.DOG.name(),
    "Descripción detallada",
    true); // core = true/false
```

### ¿Firebase no se usa para vacunas?

Correcto. En este proyecto:
- **Firebase**: Solo para autenticación de usuarios
- **H2/PostgreSQL**: Almacenamiento de datos de mascotas, vacunas, registros

---

## 📊 Resumen de Implementación

### Archivos Creados

```
src/main/java/com/bydaffi/anypetbackend/
├── models/
│   ├── PetType.java ..................... Enum tipos de mascotas
│   ├── Dog.java ......................... Entidad perro
│   ├── Hamster.java ..................... Entidad hamster
│   ├── Turtle.java ...................... Entidad tortuga
│   ├── Rabbit.java ...................... Entidad conejo
│   ├── Parakeet.java .................... Entidad periquito
│   └── Duck.java ........................ Entidad pato
├── repository/
│   └── VaccineRepository.java ........... Repositorio JPA vacunas
├── service/
│   └── VaccineService.java .............. Lógica negocio vacunas
├── controller/
│   └── VaccineController.java ........... API REST vacunas
└── config/
    └── DataInitializer.java ............. Carga inicial datos
```

### Estadísticas

- **31 vacunas** precargadas
- **7 tipos de mascotas** soportados
- **17 vacunas core** (esenciales)
- **14 vacunas opcionales**
- **11 endpoints** API REST
- **0 dependencias** Firebase para datos (solo auth)

---

## 🎉 ¡Listo para Usar!

El sistema está completamente configurado y listo para:
1. ✅ Almacenar vacunas en base de datos relacional
2. ✅ Cargar automáticamente 31 vacunas al iniciar
3. ✅ Consultar vía API REST
4. ✅ Gestionar desde código Java con `VaccineService`
5. ✅ Relacionar con registros de vacunación de mascotas

**Próximos pasos sugeridos:**
- Crear endpoints para `VaccinationRecord` (historial de vacunación)
- Agregar autenticación Firebase a los endpoints de vacunas
- Implementar frontend para visualizar vacunas
- Agregar notificaciones de próximas vacunas vencidas
