package domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore; // Importante

@Entity
@Table(name = "stands")
public class Stand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombreStand;
    private String rango;

    @OneToOne
    @JoinColumn(name = "usuario_id")
    @JsonIgnore // Evita que Jackson se bloquee en un bucle infinito
    private Usuario portador;

    @OneToMany(mappedBy = "stand", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Habilidad> habilidades = new ArrayList<>();

    public Stand() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombreStand() { return nombreStand; }
    public void setNombreStand(String nombreStand) { this.nombreStand = nombreStand; }
    public String getRango() { return rango; }
    public void setRango(String rango) { this.rango = rango; }
    public Usuario getPortador() { return portador; }
    public void setPortador(Usuario portador) { this.portador = portador; }
    public List<Habilidad> getHabilidades() { return habilidades; }
    public void setHabilidades(List<Habilidad> habilidades) { this.habilidades = habilidades; }
}