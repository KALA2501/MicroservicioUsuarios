package com.usuarios.demo.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admin")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Admin {

    @Id
    @Column(name = "pk_id", length = 255)
    private String pkId;

    @Column(name = "nombre_completo", length = 255)
    private String nombreCompleto;
}
