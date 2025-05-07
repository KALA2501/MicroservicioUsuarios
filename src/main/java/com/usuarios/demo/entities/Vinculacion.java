package com.usuarios.demo.entities;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(VinculacionId.class)
public class Vinculacion {

    @Id
    @ManyToOne
    @JoinColumn(name = "fk_id_paciente")
    private Paciente paciente;

    @Id
    @ManyToOne
    @JoinColumn(name = "fk_id_medico")
    private Medico medico;

    private Timestamp fechaVinculado;

    @ManyToOne
    @JoinColumn(name = "fk_id_tipo_vinculacion")
    private TipoVinculacion tipoVinculacion;
}
