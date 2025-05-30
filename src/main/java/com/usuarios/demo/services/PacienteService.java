package com.usuarios.demo.services;

import com.usuarios.demo.entities.*;
import com.usuarios.demo.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;


import java.sql.Timestamp;
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
        // Buscar paciente por ID
        Paciente paciente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        // Obtener las vinculaciones del paciente
        List<Vinculacion> vinculaciones = vinculacionRepository.findByPaciente_PkId(id);

        // Si no tiene vinculaciones, lanzar excepción
        if (vinculaciones.isEmpty()) {
            throw new RuntimeException("No existen vinculaciones para este paciente.");
        }

        // Eliminar todas las vinculaciones con los médicos
        for (Vinculacion vinculacion : vinculaciones) {
            Medico medico = vinculacion.getMedico();
            System.out.println("Médico asociado: " + medico.getNombre());

            // Eliminar la vinculación del paciente con el médico
            vinculacionRepository.delete(vinculacion);
        }

        // Finalmente, eliminar el paciente
        repository.delete(paciente);
        System.out.println("Paciente y sus vinculaciones eliminados con éxito.");
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

    public Paciente guardarConValidacion(Paciente paciente, Medico medico) {
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

        // Guardar el paciente
        Paciente nuevoPaciente = repository.save(paciente);

        // Crear la vinculación con el médico
        if (medico != null) {
            Vinculacion vinculacion = new Vinculacion();
            vinculacion.setPaciente(nuevoPaciente);
            vinculacion.setMedico(medico);
            vinculacion.setFechaVinculado(new Timestamp(System.currentTimeMillis()));
            vinculacionRepository.save(vinculacion);
        }

        return nuevoPaciente;
    }

    public Paciente guardarConValidacion(Paciente paciente, Medico medico, TipoVinculacion tipoVinculacion) {
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

        // Guardar el paciente
        Paciente nuevoPaciente = repository.save(paciente);

        // Crear la vinculación con el médico y el tipo de vinculación
        if (medico != null && tipoVinculacion != null) {
            Vinculacion vinculacion = new Vinculacion();
            vinculacion.setPaciente(nuevoPaciente);
            vinculacion.setMedico(medico);
            vinculacion.setTipoVinculacion(tipoVinculacion);
            vinculacion.setFechaVinculado(new Timestamp(System.currentTimeMillis()));
            vinculacionRepository.save(vinculacion);
        }

        return nuevoPaciente;
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

    public Paciente buscarPorCorreo(String email) {
        System.out.println("📧 Buscando paciente con correo: " + email);
        return repository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));
    }


    public List<Paciente> obtenerPorCentroMedico(Long idCentro) {
    return repository.findByCentroMedico_PkId(idCentro);
    }
    
    @Transactional
    public void eliminarPorCorreo(String correo) {
    Optional<Paciente> pacienteOpt = repository.findByEmail(correo);

    if (pacienteOpt.isEmpty()) {
        System.out.println("⚠️ No se encontró paciente con correo: " + correo);
        return; // o lanza una excepción si prefieres
    }

    Paciente paciente = pacienteOpt.get();

    List<Vinculacion> vinculaciones = vinculacionRepository.findByPaciente_PkId(paciente.getPkId());

    for (Vinculacion vinculacion : vinculaciones) {
        vinculacionRepository.delete(vinculacion);
    }

    repository.delete(paciente);
    System.out.println("🗑️ Paciente eliminado de la base de datos y sus vinculaciones también.");
}

}
