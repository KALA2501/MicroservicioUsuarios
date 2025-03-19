package entities;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class CentroMedico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(hidden = true)
    private Long pkId;
    private String nombre;
    @Column(unique = true, nullable = false)
    private String telefono;
    private String direccion;
    private String URLogo;
}

