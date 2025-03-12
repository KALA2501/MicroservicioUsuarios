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

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor


public class CentroMedico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAdmin; 

    private String nombre;
    private String centroMedico;

    @Column(unique = true, nullable = false)
    private String correo;

    private String telefono;
    private String direccion;
    private String contrasena;

    private String URLLogo;

    private boolean activado = false; 
}
