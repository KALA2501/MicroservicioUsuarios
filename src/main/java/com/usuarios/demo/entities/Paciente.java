package com.usuarios.demo.entities;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Paciente {

    @Id
    @Column(name = "pk_id", length = 50)
    private String pkId;

    @ManyToOne
    @JoinColumn(name = "FK_ID_CentroMedico")
    private CentroMedico centroMedico;

    private String nombre;
    private String apellido;

    @ManyToOne
    @JoinColumn(name = "FK_ID_TipoDocumento")
    private TipoDocumento tipoDocumento;

    @Column(name = "id_documento", unique = true, nullable = false)
    private String idDocumento;

    @Column(name = "fecha_nacimiento")
    private Timestamp fechaNacimiento;

    @Column(name = "codigoCIE")
    private String codigoCIE;

    @Column(unique = true, nullable = false)
    private String telefono;

    @Column(name = "email")
    private String email;
    private String direccion;
    private int etapa;
    private String zona;
    private String distrito;
    private String genero;

    @Column(name = "url_imagen")
    private String urlImagen;
}
