package com.usuarios.demo.services;

import com.usuarios.demo.entities.*;
import com.usuarios.demo.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class VinculacionService {

    private static final Logger logger = LoggerFactory.getLogger(VinculacionService.class);

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

    // 🔥 NUEVO MÉTODO: Listar pacientes vinculados a un médico (con estructura más detallada)
    public List<Map<String, String>> obtenerPacientesDeMedico(String medicoId) {
    Medico medico = medicoRepository.findById(medicoId)
        .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

    List<Vinculacion> vinculaciones = vinculacionRepository.findByMedico(medico);

    return vinculaciones.stream().map(v -> {
        Paciente p = v.getPaciente();
        Map<String, String> map = new HashMap<>();
        map.put("nombre", p.getNombre());
        map.put("apellido", p.getApellido());
        map.put("documento", p.getIdDocumento());
        map.put("telefono", p.getTelefono());
        return map;
    }).toList();
}

    public Vinculacion crearVinculacion(String pacienteId, String medicoId, String tipoVinculacionId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        TipoVinculacion tipo = tipoVinculacionRepository.findById(tipoVinculacionId)
                .orElseThrow(() -> new RuntimeException("Tipo de vinculación no encontrado"));

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

        TipoVinculacion nuevoTipo = tipoVinculacionRepository.findById(nuevoTipoVinculacionId)
                .orElseThrow(() -> new RuntimeException("Tipo de vinculación no encontrado"));

        vinculacion.setTipoVinculacion(nuevoTipo);

        logger.info("🔔 Notificación enviada al médico: " + vinculacion.getMedico().getNombre());
        logger.info("🔔 Notificación enviada al paciente: " + vinculacion.getPaciente().getNombre());

        return vinculacionRepository.save(vinculacion);
    }

    public void eliminarVinculacion(String pacienteId, String medicoId) {
        VinculacionId vinculacionId = new VinculacionId(pacienteId, medicoId);
        Vinculacion vinculacion = vinculacionRepository.findById(vinculacionId)
                .orElseThrow(() -> new RuntimeException("Vinculación no encontrada"));

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
