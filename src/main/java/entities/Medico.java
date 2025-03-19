package entities;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Medico {
    @Id
    private String pkId;
    
    @ManyToOne
    @JoinColumn(name = "FK_ID_CentroMedico")
    private CentroMedico centroMedico;

    private String nombre;
    private String apellido;

    @ManyToOne
    @JoinColumn(name = "FK_ID_TipoDocumento")
    private TipoDocumento tipoDocumento;

    @Column(unique = true, nullable = false)
    private String idDocumento;
    private Timestamp fechaNacimiento;
    private String profesion;
    private String especialidad;
    @Column(unique = true, nullable = false)
    private String telefono;
    private String direccion;
    private String genero;
    private String tarjetaProfesional;
    private String urlImagen;
}