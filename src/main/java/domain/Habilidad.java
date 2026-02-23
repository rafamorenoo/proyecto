package domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "habilidades")
public class Habilidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombreTecnica;
    private int daño;

    @ManyToOne
    @JoinColumn(name = "stand_id")
    @JsonIgnore // Importante: evita que Jackson vuelva al Stand infinitamente
    private Stand stand;

    public Habilidad() {}

    // Constructor útil para el registro rápido
    public Habilidad(String nombreTecnica, int daño, Stand stand) {
        this.nombreTecnica = nombreTecnica;
        this.daño = daño;
        this.stand = stand;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombreTecnica() { return nombreTecnica; }
    public void setNombreTecnica(String nombreTecnica) { this.nombreTecnica = nombreTecnica; }
    public int getDaño() { return daño; }
    public void setDaño(int daño) { this.daño = daño; }
    public Stand getStand() { return stand; }
    public void setStand(Stand stand) { this.stand = stand; }
}