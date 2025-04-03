package services;

import entities.SolicitudCentroMedico;
import repositories.SolicitudCentroMedicoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class SolicitudCentroMedicoService {

    @Autowired
    private SolicitudCentroMedicoRepository repository;

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
            // Intentar obtener el usuario existente
            UserRecord existingUser = FirebaseAuth.getInstance().getUserByEmail(solicitud.getCorreo());
            
            // Si existe, solo actualizamos los claims (rol)
            Map<String, Object> claims = new HashMap<>();
            claims.put("rol", rol);
            FirebaseAuth.getInstance().setCustomUserClaims(existingUser.getUid(), claims);
            
            System.out.println("✅ Usuario existente actualizado con rol: " + rol);
            
        } catch (com.google.firebase.auth.FirebaseAuthException e) {
            String errorCode = e.getAuthErrorCode().name();
            System.out.println("Código de error Firebase: " + errorCode);
            
            if (errorCode.equals("USER_NOT_FOUND")) {
                try {
                    // Solo si no existe, lo creamos
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
                        // Si el email existe, intentamos obtener el usuario y actualizar su rol
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
                        throw new RuntimeException("No se pudo crear el usuario en Firebase: " + ex.getMessage());
                    }
                }
            } else if (errorCode.equals("EMAIL_EXISTS")) {
                // Si el email existe, intentamos obtener el usuario y actualizar su rol
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
    }

    public void revertirProcesado(Long id) {
        SolicitudCentroMedico s = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        s.setProcesado(false);
        repository.save(s);
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
