package com.usuarios.demo.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tipo_vinculacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoVinculacion {

    @Id
    @Column(length = 50)
    @Schema(description = "Identificador del tipo de vinculación. Ej: 'TV01'")
    private String id;

    @Column(length = 100)
    @Schema(description = "Tipo de vinculación. Ej: 'MEDICO', 'PACIENTE'")
    private String tipo;

    @Column(length = 255)
    @Schema(description = "Descripción del tipo de vinculación")
    private String descripcion;
}
