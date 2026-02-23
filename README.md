# ⚡ SpeedwagonFoundation DB

Aplicación Java de gestión de portadores de Stand del universo JoJo's Bizarre Adventure, combinando persistencia relacional con **Hibernate/MySQL** y persistencia documental con **MongoDB**, integradas a través de una **API REST** con panel web.

---

## 📐 Modelo de Datos

```
┌──────────────┐       ┌──────────────┐       ┌─────────────────┐
│   Usuario    │ 1───1 │    Stand     │ 1───N │    Habilidad    │
│──────────────│       │──────────────│       │─────────────────│
│ id (PK)      │       │ id (PK)      │       │ id (PK)         │
│ nombre       │       │ nombreStand  │       │ nombreTecnica   │
│ linaje       │       │ rango        │       │ daño            │
│              │       │ usuario_id   │       │ stand_id (FK)   │
└──────────────┘       └──────────────┘       └─────────────────┘
```

**Ejemplo de documento JSON en MongoDB (colección `logs`):**
```json
{
  "_id": { "$oid": "64f1a2b3c4d5e6f7a8b9c0d1" },
  "accion": "REGISTRO",
  "detalles": "Se ha creado a Giorno Giovanna con el stand Gold Experience",
  "fecha": { "$date": "2025-05-10T14:32:00Z" }
}
```

---

## ✨ Características y Funcionalidades

### 1. Persistencia Relacional (SQL + Hibernate)
El núcleo de la aplicación reside en una base de datos MySQL, gestionada mediante Hibernate/JPA.
* **Modelo de Datos Completo:**
  * **Usuario:** Entidad principal (Portador).
  * **Stand:** Relación **1:1** con Usuario.
  * **Habilidad:** Relación **1:N** con Stand.
* **Operaciones CRUD:** Implementación completa para crear, leer, actualizar y eliminar portadores y sus respectivos Stands/Habilidades.
* **Transaccionalidad:** Uso de `Transaction` para asegurar que las operaciones complejas (como actualizar un usuario y sus habilidades simultáneamente) sean atómicas.
* **Consultas Avanzadas:**
  * Filtros específicos por linaje (ej. "Joestar").
  * Búsqueda de portadores poderosos (Daño > 80) mediante **HQL con JOIN**.

```java
// Consulta 1 — Filtro por linaje
"FROM Usuario WHERE linaje = :l"

// Consulta 2 — JOIN con filtro numérico en entidad anidada
"SELECT u FROM Usuario u JOIN u.stand s JOIN s.habilidades h WHERE h.daño > 80"
```

### 2. Persistencia No Relacional (MongoDB)
Utilizamos MongoDB para una capa de **Auditoría de Eventos (Logging)**.
* Cada acción importante en el sistema (Registro, Edición, Eliminación) genera un documento en la colección `logs`.
* Permite un seguimiento histórico sin penalizar el rendimiento de la base de datos transaccional principal.
* **Consultas MongoDB:**
  * Últimos 20 logs ordenados por fecha descendente.
  * Exportación completa de datos SQL a JSON mediante Jackson.

### 3. Integración Híbrida
El sistema demuestra una integración clara entre ambos mundos:

| Operación | SQL (Hibernate) | MongoDB (log) |
|---|---|---|
| Crear personaje | INSERT en `usuarios`, `stands`, `habilidades` | Log `REGISTRO` |
| Editar personaje | UPDATE en las 3 tablas | Log `EDICION` |
| Eliminar personaje | DELETE en cascada | Log `ELIMINACION` |
| Error en cualquier op. | ROLLBACK | Log `ERROR` |

Al realizar cambios en SQL, el `SpeedwagonService` dispara automáticamente un evento de persistencia en MongoDB, asegurando la trazabilidad total del flujo de datos. **El log solo se escribe si la transacción SQL tiene éxito.**

### 4. Exportación de Datos
Funcionalidad para exportar la base de datos SQL a un archivo **JSON** legible, facilitando copias de seguridad rápidas y portabilidad.

---

## 🛠 Tecnologías Utilizadas

* **Java 21** (OpenJDK).
* **Hibernate 6.4.4.Final**: Para el mapeo objeto-relacional (ORM).
* **MySQL**: Base de datos relacional principal.
* **MongoDB 5.0.1**: Para el almacenamiento de logs y auditoría.
* **Javalin 6.1.3**: Framework ligero para la creación de la API REST y el panel web.
* **Jackson**: Procesamiento y mapeo de datos JSON.
* **Maven**: Gestión de dependencias y construcción del proyecto.

---

## 🏗 Estructura del Proyecto

El proyecto sigue una arquitectura por capas para garantizar el mantenimiento y la escalabilidad:

* **`domain`**: Clases de entidad (POJOs) con anotaciones JPA (`Usuario`, `Stand`, `Habilidad`).
* **`repository`**: Capa de acceso a datos. Contiene `HibernateUtil` y `JojoRepository` (consultas HQL).
* **`service`**: Capa de lógica de negocio (`SpeedwagonService`). Gestión de la integración entre SQL y NoSQL.
* **`mongo`**: Modelo de documento MongoDB (`EventoLog`).
* **`ui`**: Interfaz de usuario y API REST utilizando Javalin.

> La UI **nunca** accede directamente a Hibernate ni a MongoDB. Todo pasa por `SpeedwagonService`.

---

## ⚙️ Requisitos e Instalación

### Bases de Datos

1. Tener **MySQL** corriendo con una base de datos llamada `speedwagon_db`.
2. Tener **MongoDB** activo en el puerto `27017`.

```sql
CREATE DATABASE speedwagon_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Configuración

Revisar el archivo `hibernate.cfg.xml` para asegurar que las credenciales coinciden con tu entorno local:

```xml
<hibernate-configuration>
  <session-factory>
    <property name="hibernate.connection.driver_class">com.mysql.cj.jdbc.Driver</property>
    <property name="hibernate.connection.url">jdbc:mysql://localhost:3306/speedwagon_db</property>
    <property name="hibernate.connection.username">root</property>
    <property name="hibernate.connection.password">TU_PASSWORD</property>
    <property name="hibernate.dialect">org.hibernate.dialect.MySQL8Dialect</property>
    <property name="hibernate.hbm2ddl.auto">update</property>
    <property name="hibernate.show_sql">true</property>

    <mapping class="domain.Usuario"/>
    <mapping class="domain.Stand"/>
    <mapping class="domain.Habilidad"/>
  </session-factory>
</hibernate-configuration>
```

> Las tablas se crean automáticamente con `hbm2ddl.auto=update`.

### Ejecución

Para compilar y ejecutar el proyecto, utiliza los siguientes comandos de Maven:

```bash
mvn clean install
mvn exec:java -Dexec.mainClass="ui.RestApi"
```

La aplicación arranca en **http://localhost:7070**

---

## 🌐 Endpoints de la API REST

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/` | Panel web de gestión |
| `GET` | `/personajes` | Listar todos los portadores |
| `GET` | `/personajes/buscar?linaje=Joestar` | Filtrar por linaje |
| `GET` | `/personajes/poderosos` | Portadores con daño > 80 |
| `POST` | `/personajes` | Crear nuevo portador |
| `PUT` | `/personajes/{id}` | Editar portador |
| `DELETE` | `/personajes/{id}` | Eliminar portador |
| `GET` | `/logs` | Ver auditoría MongoDB |
| `GET` | `/exportar` | Exportar usuarios a JSON |

---
