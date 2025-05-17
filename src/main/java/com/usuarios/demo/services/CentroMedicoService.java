package com.usuarios.demo.services;

import com.usuarios.demo.entities.*;
import com.usuarios.demo.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import java.util.HashMap;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.usuarios.demo.exceptions.CentroMedicoException;

@Service
public class CentroMedicoService {
    private static final Logger logger = LoggerFactory.getLogger(CentroMedicoService.class);

    @Autowired
    private CentroMedicoRepository repository;

    public List<CentroMedico> obtenerTodos() {
        return repository.findAll();
    }

    public Optional<CentroMedico> obtenerPorId(Long id) {
        return repository.findById(id);
    }

    public CentroMedico guardar(CentroMedico centroMedico) {
        return repository.save(centroMedico);
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }

    public CentroMedico actualizar(Long id, CentroMedico nuevosDatos) {
        Optional<CentroMedico> existente = repository.findById(id);
        if (existente.isPresent()) {
            CentroMedico centro = existente.get();
            centro.setNombre(nuevosDatos.getNombre());
            centro.setDireccion(nuevosDatos.getDireccion());
            centro.setTelefono(nuevosDatos.getTelefono());
            centro.setUrlLogo(nuevosDatos.getUrlLogo());
            return repository.save(centro);
        } else {
            throw new CentroMedicoException("Centro médico no encontrado con ID: " + id);
        }
    }

    public boolean existePorCorreo(String correo) {
        return repository.existsByCorreo(correo);
    }

    @Transactional
    public CentroMedico registrarCentroMedico(CentroMedico centro) {
        // Validar campos obligatorios
        if (centro.getNombre() == null || centro.getCorreo() == null || centro.getTelefono() == null) {
            throw new CentroMedicoException("Faltan datos obligatorios");
        }

        // Verificar si el correo ya existe
        if (repository.existsByCorreo(centro.getCorreo())) {
            throw new CentroMedicoException("Centro ya existe con ese correo");
        }

        // Guardar primero en la base de datos
        CentroMedico guardado = repository.save(centro);

        try {
            // Crear usuario en Firebase
            logger.info("FirebaseAuth instance: {}", FirebaseAuth.getInstance());
            logger.info("Invocando createUser en FirebaseAuth con correo: {}", guardado.getCorreo());
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(guardado.getCorreo())
                    .setPassword("KalaTemporal123") // Contraseña temporal
                    .setEmailVerified(false)
                    .setDisabled(false);

            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);

            // Asignar custom claim correctamente ("rol")
            Map<String, Object> claims = new HashMap<>();
            claims.put("rol", "centro_medico");

            FirebaseAuth.getInstance().setCustomUserClaims(userRecord.getUid(), claims);

            logger.info("✅ Usuario creado y rol asignado correctamente: {}", guardado.getCorreo());
        } catch (Exception e) {
            logger.error("❌ Error al crear usuario en Firebase. Revirtiendo centro médico en la base de datos...");

            // Rollback manual: eliminar el centro médico que guardamos si Firebase falla
            repository.deleteById(guardado.getPkId());

            String errorMessage = "Error al registrar centro médico: " + (e.getMessage() != null ? e.getMessage() : "Error desconocido") +
                                  "\nDetalles: " + e.toString();
            throw new CentroMedicoException(errorMessage, e);
        }

        return guardado;
    }

    @Transactional
    public void eliminarPorCorreo(String correo) {
        try {
            // Buscar el centro médico por correo
            Optional<CentroMedico> centro = repository.findByCorreo(correo);
            if (centro.isPresent()) {
                repository.delete(centro.get());
                logger.info("✅ Centro médico eliminado correctamente: {}", correo);
            } else {
                logger.warn("⚠️ No se encontró centro médico con el correo: {}", correo);
            }
        } catch (Exception e) {
            logger.error("❌ Error al eliminar centro médico: {}", e.getMessage(), e);
            throw new CentroMedicoException("Error al eliminar centro médico con correo: " + correo, e);
        }
    }

}

