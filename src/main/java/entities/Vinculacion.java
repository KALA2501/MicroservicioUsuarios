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

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

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
