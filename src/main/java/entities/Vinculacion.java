package entities;

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
    @JoinColumn(name = "PK_FK_IDPaciente")
    private Paciente paciente;

    @Id
    @ManyToOne
    @JoinColumn(name = "PK_FK_IDMedico")
    private Medico medico;

    private Timestamp fechaVinculado;

    @ManyToOne
    @JoinColumn(name = "FK_TipoVinculacion")
    private TipoVinculacion tipoVinculacion;
}
