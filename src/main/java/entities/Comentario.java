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

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Comentario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(hidden = true)
    private Long pkId;

    @ManyToOne
    @JoinColumn(name = "FK_IDMedico")
    private Medico medico;
    
    @ManyToOne
    @JoinColumn(name = "FK_IDPaciente")
    private Paciente paciente;

    @Column(nullable = false)
    private String descripcion;
    private Timestamp fecha;
}