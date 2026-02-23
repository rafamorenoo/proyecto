package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.*;
import domain.*;
import repository.JojoRepository;
import org.bson.Document;
import org.hibernate.Session;
import org.hibernate.Transaction;
import repository.HibernateUtil;

import java.io.File;
import java.util.List;
import java.util.Date;
import static com.mongodb.client.model.Sorts.descending;

public class SpeedwagonService {
    private JojoRepository repoSQL = new JojoRepository();
    private MongoDatabase db;

    public SpeedwagonService(MongoDatabase db) {
        this.db = db;
    }

    public void registrarNuevoUsuario(String nombre, String linaje, String nombreStand, String rango, String tecnica, int daño) {
        Usuario u = new Usuario(nombre, linaje);
        Stand s = new Stand();
        s.setNombreStand(nombreStand);
        s.setRango(rango);
        s.setPortador(u);
        u.setStand(s);

        Habilidad h = new Habilidad(tecnica, daño, s);
        s.getHabilidades().add(h);

        try {
            repoSQL.guardar(u); // Si falla, lanza RuntimeException
            registrarLog("REGISTRO", "Se ha creado a " + nombre + " con el stand " + nombreStand);
            System.out.println("✅ Usuario " + nombre + " creado y log registrado.");
        } catch (Exception e) {
            // SQL falló → log de error, no de éxito
            registrarLog("ERROR", "Fallo al crear " + nombre + ": " + e.getMessage());
            System.err.println("❌ ERROR AL CREAR: " + e.getMessage());
            throw e; // Re-lanzamos para que el endpoint devuelva 500
        }
    }

    public void actualizarUsuario(long id, String nombre, String linaje, String nombreStand, String rango, String tecnica, int daño) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Usuario u = session.get(Usuario.class, id);

            if (u != null) {
                u.setNombre(nombre);
                u.setLinaje(linaje);

                Stand s = u.getStand();
                if (s == null) {
                    s = new Stand();
                    s.setPortador(u);
                    u.setStand(s);
                }
                s.setNombreStand(nombreStand);
                s.setRango(rango);

                // Con orphanRemoval=true en Stand, el clear() ahora SÍ borra las filas antiguas en BD
                s.getHabilidades().clear();
                session.flush();

                Habilidad h = new Habilidad(tecnica, daño, s);
                s.getHabilidades().add(h);

                session.merge(u);
                tx.commit(); // SQL confirmado

                // Log solo si el commit fue exitoso
                registrarLog("EDICION", "Actualizado ID " + id + ": " + nombre);
                System.out.println(" Usuario " + id + " actualizado y log registrado.");
            } else {
                tx.rollback();
                System.err.println(" No se encontró usuario con ID: " + id);
            }
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            // Log de error, no de éxito
            registrarLog("ERROR", "Fallo al actualizar ID " + id + ": " + e.getMessage());
            System.err.println(" ERROR AL ACTUALIZAR: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar usuario", e); // Re-lanzamos para el endpoint
        }
    }

    public void borrarUsuario(Long id) {
        try {
            repoSQL.eliminar(id); // Si falla, lanza RuntimeException
            registrarLog("ELIMINACION", "Eliminado ID de usuario: " + id);
            System.out.println(" Usuario " + id + " eliminado y log registrado.");
        } catch (Exception e) {
            registrarLog("ERROR", "Fallo al eliminar ID " + id + ": " + e.getMessage());
            System.err.println(" ERROR AL ELIMINAR: " + e.getMessage());
            throw e;
        }
    }

    public void exportarUsuariosAJson() {
        try {
            List<Usuario> lista = repoSQL.listarTodos();
            ObjectMapper mapper = new ObjectMapper();
            File archivo = new File("usuarios_backup.json");
            mapper.writerWithDefaultPrettyPrinter().writeValue(archivo, lista);
            registrarLog("BACKUP", "Copia de seguridad JSON generada con " + lista.size() + " usuarios");
        } catch (Exception e) {
            registrarLog("ERROR", "Fallo al exportar JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String obtenerLogsComoTexto() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- AUDITORÍA DE EVENTOS (MONGODB) ---\n");
        try {
            MongoCollection<Document> col = db.getCollection("logs");
            for (Document doc : col.find().sort(descending("fecha")).limit(20)) {
                sb.append(String.format("[%s] %s: %s\n",
                        doc.get("fecha"), doc.getString("accion"), doc.getString("detalles")));
            }
        } catch (Exception e) {
            sb.append("Error al leer de MongoDB: " + e.getMessage());
        }
        return sb.toString();
    }

    public void registrarLog(String accion, String detalles) {
        try {
            MongoCollection<Document> col = db.getCollection("logs");
            Document log = new Document()
                    .append("accion", accion)
                    .append("detalles", detalles)
                    .append("fecha", new Date());
            col.insertOne(log);
        } catch (Exception e) {
            System.err.println(" Error en Log Mongo: " + e.getMessage());
        }
    }
}