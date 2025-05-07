package com.usuarios.demo.entities;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "medico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Medico {

    @Id
    @Column(name = "pk_id", length = 255)
    private String pkId;

    @ManyToOne
    @JoinColumn(name = "fk_id_centro_medico") 
    private CentroMedico centroMedico;

    private String nombre;
    private String apellido;

    @ManyToOne
    @JoinColumn(name = "fk_id_tipo_documento") 
    private TipoDocumento tipoDocumento;

    @Column(name = "id_documento", unique = true, nullable = false)
    private String idDocumento;

    @Column(name = "fecha_nacimiento")
    private Timestamp fechaNacimiento;

    private String profesion;
    private String especialidad;

    @Column(unique = true, nullable = false)
    private String telefono;

    private String direccion;
    private String genero;

    @Column(name = "tarjeta_profesional")
    private String tarjetaProfesional;

    @Column(name = "url_imagen")
    private String urlImagen;

    @Column(nullable = false)
    private String correo;
}
