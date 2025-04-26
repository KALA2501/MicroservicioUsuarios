package com.usuarios.demo.services;

import com.usuarios.demo.entities.*;
import com.usuarios.demo.repositories.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import com.google.firebase.auth.FirebaseAuthException;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SolicitudCentroMedicoService {

    @Autowired
    private SolicitudCentroMedicoRepository repository;

    @Autowired
    private CentroMedicoRepository centroMedicoRepository;

    public SolicitudCentroMedico guardarSolicitud(SolicitudCentroMedico solicitud) {
        if (repository.existsByCorreo(solicitud.getCorreo())) {
            throw new RuntimeException("Ya existe una solicitud con ese correo");
        }
        if (repository.existsByTelefono(solicitud.getTelefono())) {
            throw new RuntimeException("Ya existe una solicitud con ese teléfono");
        }

        return repository.save(solicitud);
    }

    public List<SolicitudCentroMedico> obtenerSolicitudes() {
        return repository.findAll();
    }

    public void marcarComoProcesado(Long id) {
        SolicitudCentroMedico s = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        s.setProcesado(true);
        repository.save(s);
    }

    public void procesarYCrearUsuario(Long id, String rol) {
        SolicitudCentroMedico solicitud = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        solicitud.setProcesado(true);
        repository.save(solicitud);

        try {
            UserRecord existingUser = FirebaseAuth.getInstance().getUserByEmail(solicitud.getCorreo());
            Map<String, Object> claims = new HashMap<>();
            claims.put("rol", rol);
            FirebaseAuth.getInstance().setCustomUserClaims(existingUser.getUid(), claims);
            System.out.println("✅ Usuario existente actualizado con rol: " + rol);
        } catch (com.google.firebase.auth.FirebaseAuthException e) {
            String errorCode = e.getAuthErrorCode().name();
            System.out.println("Código de error Firebase: " + errorCode);

            if (errorCode.equals("USER_NOT_FOUND")) {
                try {
                    UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                            .setEmail(solicitud.getCorreo())
                            .setPassword("KalaTemporal123")
                            .setEmailVerified(false)
                            .setDisabled(false);

                    UserRecord nuevoUsuario = FirebaseAuth.getInstance().createUser(request);

                    Map<String, Object> claims = new HashMap<>();
                    claims.put("rol", rol);
                    FirebaseAuth.getInstance().setCustomUserClaims(nuevoUsuario.getUid(), claims);

                    System.out.println("✅ Nuevo usuario creado con rol: " + rol);
                } catch (com.google.firebase.auth.FirebaseAuthException ex) {
                    String createErrorCode = ex.getAuthErrorCode().name();
                    System.out.println("Error al crear usuario: " + createErrorCode);

                    if (createErrorCode.equals("EMAIL_EXISTS")) {
                        try {
                            UserRecord user = FirebaseAuth.getInstance().getUserByEmail(solicitud.getCorreo());
                            Map<String, Object> claims = new HashMap<>();
                            claims.put("rol", rol);
                            FirebaseAuth.getInstance().setCustomUserClaims(user.getUid(), claims);
                            System.out.println("✅ Usuario existente actualizado con rol: " + rol);
                        } catch (com.google.firebase.auth.FirebaseAuthException finalEx) {
                            throw new RuntimeException(
                                    "No se pudo actualizar el usuario existente: " + finalEx.getMessage());
                        }
                    } else {
                        throw new RuntimeException("No se pudo crear el usuario en Firebase: " + ex.getMessage());
                    }
                }
            } else if (errorCode.equals("EMAIL_EXISTS")) {
                try {
                    UserRecord user = FirebaseAuth.getInstance().getUserByEmail(solicitud.getCorreo());
                    Map<String, Object> claims = new HashMap<>();
                    claims.put("rol", rol);
                    FirebaseAuth.getInstance().setCustomUserClaims(user.getUid(), claims);
                    System.out.println("✅ Usuario existente actualizado con rol: " + rol);
                } catch (com.google.firebase.auth.FirebaseAuthException finalEx) {
                    throw new RuntimeException("No se pudo actualizar el usuario existente: " + finalEx.getMessage());
                }
            } else {
                e.printStackTrace();
                throw new RuntimeException("No se pudo procesar el usuario en Firebase: " + e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error inesperado al procesar usuario: " + e.getMessage());
        }

        if (rol.equalsIgnoreCase("CENTRO_MEDICO")) {
            CentroMedico nuevo = new CentroMedico();
            nuevo.setNombre(solicitud.getNombre());
            nuevo.setCorreo(solicitud.getCorreo());
            nuevo.setDireccion(solicitud.getDireccion());
            nuevo.setTelefono(solicitud.getTelefono());
            nuevo.setUrlLogo(solicitud.getUrlLogo());

            centroMedicoRepository.save(nuevo);
            System.out.println("✅ Centro médico guardado en BD: " + nuevo.getCorreo());
        }
    }

    @Transactional
    public void revertirProcesado(Long id) {
        SolicitudCentroMedico solicitud = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        System.out.println("🔄 Iniciando proceso de reversión para: " + solicitud.getCorreo());

        // 1. Verificar si existe en centro_medico
        boolean existeEnBaseDatos = centroMedicoRepository.existsByCorreo(solicitud.getCorreo());
        System.out.println(existeEnBaseDatos ? "✅ Existe en base de datos" : "⚠️ No existe en base de datos");

        // 2. Verificar si existe en Firebase
        boolean existeEnFirebase = false;
        try {
            UserRecord user = FirebaseAuth.getInstance().getUserByEmail(solicitud.getCorreo());
            existeEnFirebase = true;
            System.out.println("✅ Existe en Firebase");
        } catch (FirebaseAuthException e) {
            System.out.println("⚠️ No existe en Firebase");
        }

        // 3. Realizar las acciones necesarias
        if (existeEnBaseDatos || existeEnFirebase) {
            // Si existe en algún lado, procedemos a eliminar
            try {
                if (existeEnBaseDatos) {
                    centroMedicoRepository.deleteByCorreo(solicitud.getCorreo());
                    System.out.println("✅ Eliminado de la base de datos");
                }

                if (existeEnFirebase) {
                    UserRecord user = FirebaseAuth.getInstance().getUserByEmail(solicitud.getCorreo());
                    FirebaseAuth.getInstance().deleteUser(user.getUid());
                    System.out.println("✅ Eliminado de Firebase");
                }

                // Marcar como no procesado
                solicitud.setProcesado(false);
                repository.save(solicitud);
                System.out.println("✅ Solicitud marcada como no procesada");

            } catch (Exception e) {
                System.err.println("❌ Error durante la reversión: " + e.getMessage());
                throw new RuntimeException("Error durante la reversión: " + e.getMessage());
            }
        } else {
            // Si no existe en ningún lado, solo actualizamos el estado
            solicitud.setProcesado(false);
            repository.save(solicitud);
            System.out.println("✅ Solicitud marcada como no procesada (no existía en ninguna base de datos)");
        }
    }

    public void eliminarSolicitud(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Solicitud no encontrada");
        }
        repository.deleteById(id);
    }

    public void eliminarPorId(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Solicitud no encontrada");
        }
        repository.deleteById(id);
    }
}
