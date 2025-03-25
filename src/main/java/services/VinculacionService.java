package services;
import entities.*;
import repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.sql.Timestamp;



@Service
public class VinculacionService {

    @Autowired
    private VinculacionRepository vinculacionRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private TipoVinculacionRepository tipoVinculacionRepository;

    public Vinculacion crearVinculacion(String pacienteId, String medicoId, String tipoVinculacionId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        Medico medico = medicoRepository.findById(medicoId)
            .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        TipoVinculacion tipo = tipoVinculacionRepository.findById(Long.valueOf(tipoVinculacionId))
            .orElseThrow(() -> new RuntimeException("Tipo de vinculación no encontrado"));

        Vinculacion vinculacion = new Vinculacion();
        vinculacion.setPaciente(paciente);
        vinculacion.setMedico(medico);
        vinculacion.setFechaVinculado(new Timestamp(System.currentTimeMillis()));
        vinculacion.setTipoVinculacion(tipo);

        return vinculacionRepository.save(vinculacion);
    }
}

