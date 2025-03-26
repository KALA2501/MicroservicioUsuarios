package repositories;

import entities.SolicitudCentroMedico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitudCentroMedicoRepository extends JpaRepository<SolicitudCentroMedico, Long> {
    boolean existsByCorreo(String correo);
    boolean existsByTelefono(String telefono);
}
