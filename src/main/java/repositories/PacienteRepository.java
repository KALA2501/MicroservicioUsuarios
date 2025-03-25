package repositories;

import entities.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface PacienteRepository extends JpaRepository<Paciente, String> {
    
    List<Paciente> findByCentroMedicoPkId(Long idCentro);
    List<Paciente> findByNombreContainingIgnoreCase(String nombre);
    Optional<Paciente> findByTipoDocumento_IdAndIdDocumento(String tipoDocumentoId, String idDocumento);
    boolean existsByTelefono(String telefono);


}
