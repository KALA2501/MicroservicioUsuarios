package services;

import entities.*;
import repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;



@Service
public class PacienteService {

    @Autowired
    private PacienteRepository repository;

    @Autowired
    private CentroMedicoRepository centroMedicoRepository;

    @Autowired
    private TipoDocumentoRepository tipoDocumentoRepository;

    @Autowired
    private VinculacionRepository vinculacionRepository;

    public List<Paciente> obtenerTodos() {
        return repository.findAll();
    }

    public Optional<Paciente> obtenerPorId(String id) {
        return repository.findById(id);
    }

    public Paciente guardar(Paciente paciente) {
        return repository.save(paciente);
    }

    public void eliminar(String id) {
        repository.deleteById(id);
    }

    public List<Paciente> obtenerPorCentroMedico(Long idCentro) {
        return repository.findByCentroMedicoPkId(idCentro);
    }
    

    public Paciente actualizar(String id, Paciente nuevosDatos) {
        Paciente existente = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        // Validar existencia de centro médico y tipo documento si llegan como objetos con solo ID
        if (nuevosDatos.getCentroMedico() != null) {
            Long idCentro = nuevosDatos.getCentroMedico().getPkId();
            CentroMedico centro = centroMedicoRepository.findById(idCentro)
                    .orElseThrow(() -> new RuntimeException("Centro médico no encontrado"));
            existente.setCentroMedico(centro);
        }

        if (nuevosDatos.getTipoDocumento() != null) {
            String idTipo = nuevosDatos.getTipoDocumento().getId();
            TipoDocumento tipo = tipoDocumentoRepository.findById(idTipo)
                    .orElseThrow(() -> new RuntimeException("Tipo de documento no encontrado"));
            existente.setTipoDocumento(tipo);
        }

        // Actualizar campos
        existente.setNombre(nuevosDatos.getNombre());
        existente.setApellido(nuevosDatos.getApellido());
        existente.setIdDocumento(nuevosDatos.getIdDocumento());
        existente.setFechaNacimiento(nuevosDatos.getFechaNacimiento());
        existente.setCodigoCIE(nuevosDatos.getCodigoCIE());
        existente.setTelefono(nuevosDatos.getTelefono());
        existente.setEmail(nuevosDatos.getEmail());
        existente.setDireccion(nuevosDatos.getDireccion());
        existente.setEtapa(nuevosDatos.getEtapa());
        existente.setZona(nuevosDatos.getZona());
        existente.setDistrito(nuevosDatos.getDistrito());
        existente.setGenero(nuevosDatos.getGenero());
        existente.setUrlImagen(nuevosDatos.getUrlImagen());

        return repository.save(existente);
    }

    public List<Vinculacion> obtenerPacientesPorTarjetaProfesional(String tarjetaProfesional) {
        return vinculacionRepository.findByMedico_TarjetaProfesional(tarjetaProfesional);
    }

    public List<Paciente> buscarPorNombre(String nombre) {
        return repository.findByNombreContainingIgnoreCase(nombre);
    }
    
    public Optional<Paciente> buscarPorTipoYNumero(String tipoDocumentoId, String idDocumento) {
        return repository.findByTipoDocumento_IdAndIdDocumento(tipoDocumentoId, idDocumento);
    }

    public Paciente guardarConValidacion(Paciente paciente) {
        // Verificar si ya existe por tipo y número de documento
        Optional<Paciente> existentePorDoc = repository.findByTipoDocumento_IdAndIdDocumento(
            paciente.getTipoDocumento().getId(), paciente.getIdDocumento()
        );
    
        if (existentePorDoc.isPresent()) {
            throw new RuntimeException("Ya existe un paciente con ese tipo y número de documento");
        }
    
        // Verificar si ya existe el teléfono
        boolean existeTelefono = repository.existsByTelefono(paciente.getTelefono());
        if (existeTelefono) {
            throw new RuntimeException("Ya existe un paciente con ese número de teléfono");
        }
    
        return repository.save(paciente);
    }
    
    
}
