package services;

import entities.*;
import repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

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

    @Autowired
    private MedicamentosRepository medicamentoRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private JwtService jwtService;

    public List<Paciente> obtenerTodos() {
        return repository.findAll();
    }

    public Optional<Paciente> obtenerPorId(String id) {
        return repository.findById(id);
    }

    public Paciente guardar(Paciente paciente) {
        return repository.save(paciente);
    }

    @Transactional
    public void eliminar(String id) {
        System.out.println("🔎 Buscando paciente con ID: " + id);
        Paciente paciente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        System.out.println("✅ Paciente encontrado: " + paciente.getNombre());

        List<Vinculacion> vinculaciones = vinculacionRepository.findByPaciente_PkId(id);
        System.out.println("🔗 Vinculaciones encontradas: " + vinculaciones.size());

        try {
            vinculacionRepository.deleteAll(vinculaciones);
            System.out.println("✅ Vinculaciones eliminadas.");
        } catch (Exception e) {
            System.err.println("❌ Error al eliminar vinculaciones: " + e.getMessage());
        }

        try {
            repository.delete(paciente);
            System.out.println("🔥 Paciente eliminado con éxito.");
        } catch (Exception e) {
            System.err.println("❌ Error al eliminar paciente: " + e.getMessage());
            throw e;
        }
    }

    public List<Paciente> obtenerPorCentroMedico(Long idCentro) {
        return repository.findByCentroMedicoPkId(idCentro);
    }

    public Paciente actualizar(String id, Paciente nuevosDatos) {
        Paciente existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        // Validar existencia de centro médico y tipo documento si llegan como objetos
        // con solo ID
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
                paciente.getTipoDocumento().getId(), paciente.getIdDocumento());

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

    public Paciente guardarConMedicamentos(Paciente paciente, List<Medicamentos> medicamentos) {
        Paciente guardado = repository.save(paciente);

        for (Medicamentos m : medicamentos) {
            m.setPaciente(guardado);
            medicamentoRepository.save(m);
        }

        return guardado;
    }

    public Paciente actualizarMedicamentos(String pacienteId, List<Medicamentos> nuevosMedicamentos) {
        Paciente paciente = repository.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        medicamentoRepository.deleteByPaciente(paciente);

        for (Medicamentos m : nuevosMedicamentos) {
            m.setPaciente(paciente);
            medicamentoRepository.save(m);
        }

        return paciente;
    }

    public void eliminarMedicamento(Long medicamentoId) {
        Medicamentos med = medicamentoRepository.findById(medicamentoId)
                .orElseThrow(() -> new RuntimeException("Medicamento no encontrado"));

        medicamentoRepository.delete(med);
    }

    public List<Medicamentos> obtenerMedicamentosDePaciente(String pacienteId) {
        Paciente paciente = repository.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        return medicamentoRepository.findByPaciente(paciente);
    }

    public List<Paciente> obtenerPacientesDelMedico(String token) {
        String correo = jwtService.extractUsername(token);
        System.out.println("📩 Correo del médico autenticado: " + correo);

        Medico medico = medicoRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        System.out.println("🔍 Buscando vinculaciones del médico ID: " + medico.getPkId());

        List<Vinculacion> vinculaciones = vinculacionRepository.findByMedico_PkId(medico.getPkId());
        System.out.println("🔗 Cantidad de vinculaciones encontradas: " + vinculaciones.size());

        return vinculaciones.stream()
                .map(Vinculacion::getPaciente)
                .distinct()
                .collect(Collectors.toList());
    }

}
