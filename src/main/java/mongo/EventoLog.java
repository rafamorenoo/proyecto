package mongo;

import org.bson.types.ObjectId;
import java.util.Date;

public class EventoLog {
    public ObjectId id;
    public String accion; // Ej: "CREAR_USUARIO"
    public String detalles;
    public String fecha;

    public EventoLog() {}
    public EventoLog(String accion, String detalles) {
        this.accion = accion;
        this.detalles = detalles;
        this.fecha = new Date().toString();
    }
}