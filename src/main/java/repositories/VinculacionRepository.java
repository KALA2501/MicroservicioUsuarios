package repositories;

import entities.Medico;
import entities.Paciente;
import entities.TipoVinculacion;
import entities.Vinculacion;
import entities.VinculacionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VinculacionRepository extends JpaRepository<Vinculacion, VinculacionId> {
    List<Vinculacion> findByMedico_TarjetaProfesional(String tarjetaProfesional);
    List<Vinculacion> findByPaciente(Paciente paciente);
    boolean existsByPacienteAndMedicoAndTipoVinculacion(Paciente paciente, Medico medico, TipoVinculacion tipo);
    Optional<Vinculacion> findByPacienteAndMedicoAndTipoVinculacion(Paciente paciente, Medico medico, TipoVinculacion tipo);
    Optional<Vinculacion> findByPaciente_PkIdAndMedico_PkId(String pacienteId, String medicoId);
    List<Vinculacion> findByPaciente_CentroMedico_PkId(Long idCentroMedico);

}
