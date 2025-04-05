package services;
import entities.*;
import repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MedicoService {
    @Autowired
    private MedicoRepository repository;

    @Autowired
    private CentroMedicoRepository centroMedicoRepository;

    @Autowired
    private TipoDocumentoRepository tipoDocumentoRepository;

    public Optional<CentroMedico> obtenerCentroPorId(Long id) {
        return centroMedicoRepository.findById(id);
    }

    public Optional<TipoDocumento> obtenerTipoDocumentoPorId(String id) {
        return tipoDocumentoRepository.findById(id);
    }

    public List<Medico> obtenerTodos() {
        return repository.findAll();
    }

    public Optional<Medico> obtenerPorId(String id) {
        return repository.findById(id);
    }

    public Medico guardar(Medico medico) {
        if (medico.getPkId() == null || medico.getPkId().isBlank()) {
            medico.setPkId(UUID.randomUUID().toString());
        }

        if (medico.getCentroMedico() != null && medico.getCentroMedico().getPkId() != null) {
            CentroMedico centro = centroMedicoRepository.findById(medico.getCentroMedico().getPkId())
                .orElseThrow(() -> new RuntimeException("Centro médico no encontrado"));
            medico.setCentroMedico(centro);
        }

        if (medico.getTipoDocumento() != null && medico.getTipoDocumento().getId() != null) {
            TipoDocumento tipoDoc = tipoDocumentoRepository.findById(medico.getTipoDocumento().getId())
                .orElseThrow(() -> new RuntimeException("Tipo de documento no encontrado"));
            medico.setTipoDocumento(tipoDoc);
        }

        return repository.save(medico);
    }

    public void eliminar(String id) {
        repository.deleteById(id);
    }

    public List<Medico> obtenerPorCentroMedico(Long idCentro) {
        return repository.findByCentroMedicoPkId(idCentro);
    }

    public List<Medico> filtrarMedicos(String nombre, String tarjeta, String profesion) {
        if (nombre != null && !nombre.isEmpty()) {
            return repository.findByNombreContainingIgnoreCase(nombre);
        } else if (tarjeta != null && !tarjeta.isEmpty()) {
            return repository.findByTarjetaProfesionalContainingIgnoreCase(tarjeta);
        } else if (profesion != null && !profesion.isEmpty()) {
            return repository.findByProfesionContainingIgnoreCase(profesion);
        }
        return repository.findAll();
    }
}
