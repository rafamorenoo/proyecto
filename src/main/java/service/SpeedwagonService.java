package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.*;
import domain.*;
import repository.JojoRepository;
import org.bson.Document;
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

        repoSQL.guardar(u);
        registrarLog("REGISTRO", "Se ha creado a " + nombre + " con el stand " + nombreStand);
    }

    public void borrarUsuario(Long id) {
        repoSQL.eliminar(id);
        registrarLog("ELIMINACION", "Eliminado ID de usuario: " + id);
    }

    public void exportarUsuariosAJson() {
        try {
            List<Usuario> lista = repoSQL.listarTodos();
            ObjectMapper mapper = new ObjectMapper();
            File archivo = new File("usuarios_backup.json");
            mapper.writerWithDefaultPrettyPrinter().writeValue(archivo, lista);
            registrarLog("BACKUP", "Copia de seguridad JSON generada");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String obtenerLogsComoTexto() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- AUDITORÍA DE EVENTOS (MONGODB) ---\n");
        MongoCollection<Document> col = db.getCollection("logs");
        for (Document doc : col.find().sort(descending("fecha")).limit(20)) {
            sb.append(String.format("[%s] %s: %s\n",
                    doc.get("fecha"), doc.getString("accion"), doc.getString("detalles")));
        }
        return sb.toString();
    }

    private void registrarLog(String accion, String detalles) {
        try {
            MongoCollection<Document> col = db.getCollection("logs");
            Document log = new Document()
                    .append("accion", accion)
                    .append("detalles", detalles)
                    .append("fecha", new Date());
            col.insertOne(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}