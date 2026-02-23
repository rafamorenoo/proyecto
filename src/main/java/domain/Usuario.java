package domain;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String linaje;

    @OneToOne(mappedBy = "portador", cascade = CascadeType.ALL)
    private Stand stand;

    public Usuario() {}

    public Usuario(String nombre, String linaje) {
        this.nombre = nombre;
        this.linaje = linaje;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getLinaje() { return linaje; }
    public void setLinaje(String linaje) { this.linaje = linaje; }

    public Stand getStand() { return stand; }
    public void setStand(Stand stand) { this.stand = stand; }
}