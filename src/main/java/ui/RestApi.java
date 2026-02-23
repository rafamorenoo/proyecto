package ui;

import io.javalin.Javalin;
import service.SpeedwagonService;
import com.mongodb.client.MongoClients;
import repository.JojoRepository;
import java.util.Map;

public class RestApi {
    public static void main(String[] args) {
        var mongoClient = MongoClients.create("mongodb://localhost:27017");
        var database = mongoClient.getDatabase("speedwagon_db");
        SpeedwagonService service = new SpeedwagonService(database);
        JojoRepository repo = new JojoRepository();

        var app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> cors.addRule(it -> it.anyHost()));
        }).start(7070);

        app.get("/", ctx -> {
            ctx.contentType("text/html");
            ctx.result(getHtmlContent());
        });

        app.get("/personajes", ctx -> ctx.json(repo.listarTodos()));
        app.get("/personajes/buscar", ctx -> ctx.json(repo.buscarPorLinaje(ctx.queryParam("linaje"))));
        app.get("/personajes/poderosos", ctx -> ctx.json(repo.listarPoderosos()));

        app.post("/personajes", ctx -> {
            try {
                Map<String, Object> body = ctx.bodyAsClass(Map.class);
                int valorDanio = body.get("danio") != null ? Integer.parseInt(body.get("danio").toString()) : 0;
                service.registrarNuevoUsuario(
                        (String) body.get("nombre"), (String) body.get("linaje"),
                        (String) body.get("nombreStand"), (String) body.get("rango"),
                        (String) body.get("tecnica"), valorDanio
                );
                ctx.status(201).result("OK");
            } catch (Exception e) {
                ctx.status(500).result(e.getMessage());
            }
        });

        // Endpoint PUT corregido usando el Service
        app.put("/personajes/{id}", ctx -> {
            try {
                long id = Long.parseLong(ctx.pathParam("id"));
                Map<String, Object> body = ctx.bodyAsClass(Map.class);
                int valorDanio = body.get("danio") != null ? Integer.parseInt(body.get("danio").toString()) : 0;

                service.actualizarUsuario(
                        id,
                        (String) body.get("nombre"),
                        (String) body.get("linaje"),
                        (String) body.get("nombreStand"),
                        (String) body.get("rango"),
                        (String) body.get("tecnica"),
                        valorDanio
                );
                ctx.result("OK");
            } catch (Exception e) {
                ctx.status(500).result("Error al editar: " + e.getMessage());
            }
        });

        app.delete("/personajes/{id}", ctx -> {
            service.borrarUsuario(Long.parseLong(ctx.pathParam("id")));
            ctx.result("OK");
        });

        app.get("/logs", ctx -> ctx.result(service.obtenerLogsComoTexto()));
        app.get("/exportar", ctx -> { service.exportarUsuariosAJson(); ctx.result("OK"); });
    }

    private static String getHtmlContent() {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <title>Panel de Gestión Speedwagon</title>
            <style>
                body { font-family: 'Segoe UI', sans-serif; margin: 40px; background: #121212; color: #e0e0e0; }
                .container { max-width: 950px; margin: auto; background: #1e1e1e; padding: 25px; border-radius: 12px; }
                h1 { color: #f1c40f; text-align: center; }
                .panel { background: #252525; padding: 20px; border-radius: 8px; margin-bottom: 20px; border: 1px solid #333; }
                .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; margin-bottom: 15px; }
                input { background: #333; border: 1px solid #444; color: white; padding: 10px; border-radius: 4px; }
                button { cursor: pointer; padding: 10px 15px; border: none; border-radius: 4px; font-weight: bold; transition: 0.2s; }
                .btn-save { background: #27ae60; color: white; width: 100%; font-size: 1.1em; }
                .nav { display: flex; gap: 10px; margin-bottom: 20px; }
                table { width: 100%; border-collapse: collapse; }
                th, td { padding: 12px; text-align: left; border-bottom: 1px solid #333; }
                th { color: #f1c40f; }
                .btn-red { background: #e74c3c; color: white; padding: 5px 10px; }
                .btn-edit { background: #f1c40f; color: #000; padding: 5px 10px; margin-right: 5px; }
            </style>
        </head>
        <body>
            <div class="container">
                <h1>Panel de Gestión</h1>
                <div class="panel">
                    <h3 id="form-title" style="color:#f1c40f; margin-top:0;">➕ Registrar Nuevo Portador</h3>
                    <div class="form-grid">
                        <input type="text" id="nombre" placeholder="Nombre">
                        <input type="text" id="linaje" placeholder="Linaje">
                        <input type="text" id="stand" placeholder="Stand">
                        <input type="text" id="rango" placeholder="Rango">
                        <input type="text" id="tecnica" placeholder="Técnica">
                        <input type="number" id="danio" placeholder="Daño (0-100)">
                    </div>
                    <button class="btn-save" id="btn-main" onclick="crear()">Guardar</button>
                    <button id="btn-cancel" style="display:none; margin-top:10px; background:#666; color:white; width:100%" onclick="resetearFormulario()">Cancelar Edición</button>
                </div>
                <div class="nav">
                    <button style="background:#3498db; color:white;" onclick="cargar()">🔄 Ver Todos</button>
                    <button style="background:#e67e22; color:white;" onclick="filtrar('Joestar')">⭐ Solo Joestars</button>
                    <button style="background:#c0392b; color:white;" onclick="poderosos()">🔥 Daño > 80</button>
                    <button style="background:#8e44ad; color:white;" onclick="window.open('/logs')">📜 Auditoría</button>
                </div>
                <table>
                    <thead>
                        <tr><th>ID</th><th>Nombre</th><th>Linaje</th><th>Stand</th><th>Acciones</th></tr>
                    </thead>
                    <tbody id="lista"></tbody>
                </table>
            </div>
            <script>
                let idEditando = null;
                function render(data) {
                    const t = document.getElementById('lista');
                    t.innerHTML = "";
                    data.forEach(p => {
                        const strData = btoa(JSON.stringify(p));
                        t.innerHTML += `<tr>
                            <td>${p.id}</td><td><b>${p.nombre}</b></td><td>${p.linaje}</td>
                            <td>${p.stand ? p.stand.nombreStand : '-'}</td>
                            <td>
                                <button class="btn-edit" onclick="prepararEdicion('${strData}')">Editar</button>
                                <button class="btn-red" onclick="borrar(${p.id})">Eliminar</button>
                            </td>
                        </tr>`;
                    });
                }
                function prepararEdicion(base64) {
                    const p = JSON.parse(atob(base64));
                    idEditando = p.id;
                    document.getElementById('form-title').innerText = "📝 Editando Portador ID: " + idEditando;
                    document.getElementById('nombre').value = p.nombre;
                    document.getElementById('linaje').value = p.linaje;
                    document.getElementById('stand').value = p.stand ? p.stand.nombreStand : '';
                    document.getElementById('rango').value = p.stand ? p.stand.rango : '';
                    if(p.stand && p.stand.habilidades.length > 0) {
                        document.getElementById('tecnica').value = p.stand.habilidades[0].nombreTecnica;
                        document.getElementById('danio').value = p.stand.habilidades[0].daño;
                    }
                    const btn = document.getElementById('btn-main');
                    btn.innerText = "💾 Confirmar Cambios";
                    btn.style.background = "#f39c12";
                    btn.onclick = enviarEdicion;
                    document.getElementById('btn-cancel').style.display = "block";
                }
                function enviarEdicion() {
                            const payload = obtenerPayload();
                            console.log("Enviando edición para ID:", idEditando, payload);
                
                            fetch('/personajes/' + idEditando, {
                                method: 'PUT',
                                headers: {'Content-Type': 'application/json'},
                                body: JSON.stringify(payload)
                            }).then(res => {
                                if (res.ok) {
                                    alert('¡Actualizado!');
                                    resetearFormulario();
                                    cargar(); // Esto refresca la tabla
                                } else {
                                    alert('Error en el servidor');
                                }
                            });
                        }
                function obtenerPayload() {
                    return {
                        nombre: document.getElementById('nombre').value,
                        linaje: document.getElementById('linaje').value,
                        nombreStand: document.getElementById('stand').value,
                        rango: document.getElementById('rango').value,
                        tecnica: document.getElementById('tecnica').value,
                        danio: document.getElementById('danio').value
                    };
                }
                function resetearFormulario() {
                    idEditando = null;
                    document.getElementById('form-title').innerText = "➕ Registrar Nuevo Portador";
                    document.querySelectorAll('input').forEach(i => i.value = '');
                    const btn = document.getElementById('btn-main');
                    btn.innerText = "Guardar";
                    btn.style.background = "#27ae60";
                    btn.onclick = crear;
                    document.getElementById('btn-cancel').style.display = "none";
                }
                function cargar() { fetch('/personajes').then(r => r.json()).then(render); }
                function filtrar(l) { fetch('/personajes/buscar?linaje='+l).then(r => r.json()).then(render); }
                function poderosos() { fetch('/personajes/poderosos').then(r => r.json()).then(render); }
                function crear() {
                    fetch('/personajes', {
                        method: 'POST',
                        headers: {'Content-Type': 'application/json'},
                        body: JSON.stringify(obtenerPayload())
                    }).then(() => { alert('¡Guardado!'); resetearFormulario(); cargar(); });
                }
                function borrar(id) {
                    if(confirm('¿Eliminar?')) fetch('/personajes/'+id, {method:'DELETE'}).then(() => cargar());
                }
                window.onload = cargar;
            </script>
        </body>
        </html>
        """;
    }
}