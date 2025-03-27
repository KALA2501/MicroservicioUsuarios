package services;

import entities.SolicitudCentroMedico;
import repositories.SolicitudCentroMedicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;

import java.util.List;

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

    public void procesarYCrearUsuario(Long id) {
        SolicitudCentroMedico solicitud = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
    
        solicitud.setProcesado(true);
        repository.save(solicitud);
    
        try {
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(solicitud.getCorreo())
                    .setPassword("KalaTemporal123") // Contraseña temporal
                    .setEmailVerified(false)
                    .setDisabled(false);
    
            FirebaseAuth.getInstance().createUser(request);
            System.out.println("✅ Usuario creado en Firebase");
    
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("No se pudo crear el usuario en Firebase");
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
    
}
