package com.usuarios.demo.services;

import com.usuarios.demo.entities.*;
import com.usuarios.demo.repositories.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Service
public class MedicoService {

    private static final Logger logger = LoggerFactory.getLogger(MedicoService.class);

    private final MedicoRepository repository;
    private final CentroMedicoRepository centroMedicoRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final FirebaseAuth firebaseAuth;

    public MedicoService(
        MedicoRepository repository,
        CentroMedicoRepository centroMedicoRepository,
        TipoDocumentoRepository tipoDocumentoRepository,
        FirebaseAuth firebaseAuth // 👈 ¡agrégalo aquí también!
    ) {
        this.repository = repository;
        this.centroMedicoRepository = centroMedicoRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.firebaseAuth = firebaseAuth; // 👈 asignación
    }

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
        Medico medico = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        try {
            UserRecord userRecord = firebaseAuth.getUserByEmail(medico.getCorreo());
            firebaseAuth.deleteUser(userRecord.getUid());
        } catch (FirebaseAuthException e) {
            logger.error("❌ Error al eliminar usuario Firebase: " + e.getMessage(), e);
        }

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

    public Optional<Medico> obtenerPorCorreo(String correo) {
        return repository.findByCorreo(correo);
    }

    // Updated eliminarPorCorreo to include Firebase deletion logic
    public void eliminarPorCorreo(String correo) {
        Optional<Medico> medicoOpt = repository.findByCorreo(correo);

        if (medicoOpt.isEmpty()) {
            logger.warn("No se encontró médico con correo: {}", correo);
            return;
        }

        Medico medico = medicoOpt.get();

        // 1. Intentar eliminar en Firebase Authentication
        try {
            UserRecord userRecord = firebaseAuth.getUserByEmail(correo);
            firebaseAuth.deleteUser(userRecord.getUid());
            logger.info("Usuario de Firebase eliminado: {}", correo);
        } catch (FirebaseAuthException e) {
            if ("USER_NOT_FOUND".equals(e.getAuthErrorCode().name())) {
                logger.warn("Usuario ya no existe en Firebase. Continuando con la eliminación local...");
            } else {
                logger.error("Error al eliminar usuario en Firebase: {}", e.getMessage());
                return;
            }
        }

        // 2. Eliminar localmente
        repository.delete(medico);
        logger.info("Médico eliminado localmente: {}", correo);
    }

    public Medico obtenerOCrearPorCorreo(String correo) {
        return repository.findByCorreo(correo).orElseGet(() -> {
            Medico nuevoMedico = new Medico();
            nuevoMedico.setCorreo(correo);
            nuevoMedico.setPkId(UUID.randomUUID().toString());
            return repository.save(nuevoMedico);
        });
    }
}
