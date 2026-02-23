🚀 Características y Funcionalidades
1. Persistencia Relacional (SQL + Hibernate)
El núcleo de los datos reside en una base de datos MySQL, gestionada mediante Hibernate/JPA.

Modelo de Datos Completo:

Usuario: Entidad principal (Portador).

Stand: Relación 1:1 con Usuario.

Habilidad: Relación 1:N con Stand.

Operaciones CRUD: Implementación completa para crear, leer, actualizar y eliminar portadores y sus stands.

Transaccionalidad: Uso de Transaction para asegurar que las operaciones complejas (como actualizar un usuario y sus habilidades simultáneamente) sean atómicas.

Consultas Avanzadas: * Filtros específicos por linaje (ej. "Joestar").

Búsqueda de portadores poderosos (Daño > 80) mediante HQL con JOIN.

2. Persistencia No Relacional (MongoDB)
Utilizamos MongoDB para una capa de Auditoría de Eventos (Logging).

Cada acción importante en el sistema (Registro, Edición, Eliminación) genera un documento en la colección logs.

Permite un seguimiento histórico sin penalizar el rendimiento de la base de datos transaccional principal.

3. Integración Híbrida
El sistema demuestra una integración clara entre ambos mundos:

Al realizar cambios en SQL, el SpeedwagonService dispara automáticamente un evento de persistencia en MongoDB, asegurando la trazabilidad total del flujo de datos.

4. Exportación de Datos
Funcionalidad para exportar la base de datos SQL a un archivo JSON legible, facilitando copias de seguridad rápidas.

🛠️ Tecnologías Utilizadas
Java 21 (OpenJDK).

Hibernate 6.4.4.Final: Para el mapeo objeto-relacional (ORM).

MySQL: Base de datos relacional principal.

MongoDB 5.0.1: Para el almacenamiento de logs y auditoría.

Javalin 6.1.3: Framework ligero para la creación de la API REST y el panel web.

Jackson: Procesamiento de datos JSON.

Maven: Gestión de dependencias y construcción.

🏗️ Estructura del Proyecto
El proyecto sigue una arquitectura por capas para garantizar el mantenimiento y la escalabilidad:

domain: Clases de entidad (POJOs) con anotaciones JPA (Usuario, Stand, Habilidad).

repository: Capa de acceso a datos. Contiene HibernateUtil y JojoRepository (consultas HQL).

service: Capa de lógica de negocio (SpeedwagonService). Aquí se gestiona la integración entre SQL y NoSQL.

ui: Interfaz de usuario y API REST utilizando Javalin.

📋 Requisitos e Instalación
Bases de Datos:

Tener MySQL corriendo con una base de datos llamada jojo_db.

Tener MongoDB activo en el puerto 27017.

Configuración:

Revisar hibernate.cfg.xml para asegurar que las credenciales (root/password) coinciden con tu entorno.

Ejecución:

Bash
mvn clean install
mvn exec:java -Dexec.mainClass="ui.RestApi"
Acceso:

Abrir http://localhost:7070 en el navegador.
