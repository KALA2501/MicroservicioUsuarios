package com.usuarios.demo.entities;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;

@Entity
@Table(name = "paciente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Paciente {

    @Id
    @Column(name = "pk_id", length = 50)
    private String pkId;

    @ManyToOne
    @JoinColumn(name = "fk_id_centro_medico")
    private CentroMedico centroMedico;

    @ManyToOne
    @JoinColumn(name = "fk_id_tipo_documento")
    private TipoDocumento tipoDocumento;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "fk_contacto_emergencia", unique = true, nullable = false)
    private ContactoEmergencia contactoEmergencia;

    private String nombre;
    private String apellido;

    @Column(name = "id_documento", length = 100, nullable = false, unique = true)
    private String idDocumento;

    @Column(name = "fecha_nacimiento")
    private Timestamp fechaNacimiento;

    @Column(name = "codigo_cie", length = 100)
    private String codigoCIE;

    @Column(length = 100, nullable = false, unique = true)
    private String telefono;

    private String email;
    private String direccion;

    @Column(name = "etapa")
    private int etapa;

    private String zona;
    private String distrito;

    @Column(length = 50)
    private String genero;

    @Column(name = "url_imagen")
    private String urlImagen;
}
