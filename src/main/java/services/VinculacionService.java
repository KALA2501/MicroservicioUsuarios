package services;

import entities.*;
import repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

import java.util.stream.Collectors;

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

    public List<Map<String, String>> obtenerVinculacionesPorMedico(String tarjetaProfesional) {
        List<Vinculacion> vinculaciones = vinculacionRepository.findByMedico_TarjetaProfesional(tarjetaProfesional);

        return vinculaciones.stream().map(v -> {
            Map<String, String> datos = new HashMap<>();
            datos.put("nombrePaciente", v.getPaciente().getNombre() + " " + v.getPaciente().getApellido());
            datos.put("tipoRelacion", v.getTipoVinculacion().getTipo());
            return datos;
        }).collect(Collectors.toList());
    }

    public Vinculacion crearVinculacion(String pacienteId, String medicoId, String tipoVinculacionId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        TipoVinculacion tipo = tipoVinculacionRepository.findById(Long.valueOf(tipoVinculacionId))
                .orElseThrow(() -> new RuntimeException("Tipo de vinculación no encontrado"));

        // Verificar duplicado (sin usar getPkId)
        Optional<Vinculacion> existente = vinculacionRepository
                .findByPacienteAndMedicoAndTipoVinculacion(paciente, medico, tipo);

        if (existente.isPresent()) {
            throw new RuntimeException("La vinculación ya existe entre ese paciente, médico y tipo.");
        }

        Vinculacion vinculacion = new Vinculacion();
        vinculacion.setPaciente(paciente);
        vinculacion.setMedico(medico);
        vinculacion.setTipoVinculacion(tipo);
        vinculacion.setFechaVinculado(new Timestamp(System.currentTimeMillis()));

        return vinculacionRepository.save(vinculacion);
    }

    public List<Map<String, String>> obtenerVinculacionesPorPaciente(String tipoDocumentoId, String idDocumento) {
        Paciente paciente = pacienteRepository.findByTipoDocumento_IdAndIdDocumento(tipoDocumentoId, idDocumento)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        List<Vinculacion> vinculaciones = vinculacionRepository.findByPaciente(paciente);

        return vinculaciones.stream().map(v -> {
            Map<String, String> datos = new HashMap<>();
            Medico m = v.getMedico();
            datos.put("nombreMedico", m.getNombre() + " " + m.getApellido());
            datos.put("tipoRelacion", v.getTipoVinculacion().getTipo());
            return datos;
        }).collect(Collectors.toList());
    }

    public Vinculacion actualizarVinculacion(VinculacionId vinculacionId, String nuevoTipoVinculacionId) {
        Vinculacion vinculacion = vinculacionRepository.findById(vinculacionId)
                .orElseThrow(() -> new RuntimeException("Vinculación no encontrada"));

        TipoVinculacion nuevoTipo = tipoVinculacionRepository.findById(Long.valueOf(nuevoTipoVinculacionId))
                .orElseThrow(() -> new RuntimeException("Tipo de vinculación no encontrado"));

        vinculacion.setTipoVinculacion(nuevoTipo);

        // Simulación de notificación
        System.out.println("🔔 Notificación enviada al médico: " + vinculacion.getMedico().getNombre());
        System.out.println("🔔 Notificación enviada al paciente: " + vinculacion.getPaciente().getNombre());

        return vinculacionRepository.save(vinculacion);
    }

    public void eliminarVinculacion(String pacienteId, String medicoId) {
        VinculacionId vinculacionId = new VinculacionId(pacienteId, medicoId); // Crea el VinculacionId usando
                                                                               // pacienteId y medicoId

        Vinculacion vinculacion = vinculacionRepository.findById(vinculacionId)
                .orElseThrow(() -> new RuntimeException("Vinculación no encontrada"));

        // Eliminar la vinculación
        vinculacionRepository.delete(vinculacion);
    }

    public List<Map<String, String>> obtenerVinculacionesPorCentroMedico(Long idCentro) {
        List<Vinculacion> vinculaciones = vinculacionRepository.findByPaciente_CentroMedico_PkId(idCentro);

        return vinculaciones.stream().map(v -> {
            Map<String, String> datos = new HashMap<>();
            datos.put("paciente", v.getPaciente().getNombre() + " " + v.getPaciente().getApellido());
            datos.put("medico", v.getMedico().getNombre() + " " + v.getMedico().getApellido());
            datos.put("tipoRelacion", v.getTipoVinculacion().getTipo());
            datos.put("fechaVinculacion", v.getFechaVinculado().toString());
            return datos;
        }).collect(Collectors.toList());
    }

}
