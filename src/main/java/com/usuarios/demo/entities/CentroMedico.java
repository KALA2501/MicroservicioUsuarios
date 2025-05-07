package com.usuarios.demo.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "centro_medico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CentroMedico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_id")
    @Schema(hidden = true)
    private Long pkId;

    private String nombre;

    @Column(unique = true, nullable = false)
    private String telefono;

    private String direccion;

    @Column(name = "url_logo")
    private String urlLogo;

    @Column(unique = true, nullable = false)
    private String correo;
}
