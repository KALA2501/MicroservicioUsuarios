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

public class Admin {
    @Id
    private String pkId;
    private String nombreCompleto;

    @ManyToOne
    @JoinColumn(name = "FK_CentroMedico")
    private CentroMedico centroMedico;
}
