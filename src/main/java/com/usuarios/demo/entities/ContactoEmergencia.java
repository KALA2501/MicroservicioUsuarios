package com.usuarios.demo.entities;

import jakarta.persistence.*;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "contacto_emergencia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactoEmergencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(hidden = true)
    private Long pkId;

    private String nombre;
    private String apellido;
    private String relacion;
    private String direccion;

    @Column(unique = true, nullable = false)
    private String telefono;

    private String email;
}
