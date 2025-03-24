package repositories;

import entities.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, String> {
    
    List<Paciente> findByCentroMedicoPkId(Long idCentro);
}
