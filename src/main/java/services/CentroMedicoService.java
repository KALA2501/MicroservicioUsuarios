package services;
import entities.*;
import repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;



@Service
public class CentroMedicoService {
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
            centro.setURLogo(nuevosDatos.getURLogo());
            return repository.save(centro);
        } else {
            throw new RuntimeException("Centro médico no encontrado con ID: " + id);
        }
    }

    @Autowired
    private CentroMedicoRepository centroMedicoRepository;
    public CentroMedico registrarCentroMedico(CentroMedico centro) {
        // Validaciones previas
        if (centroMedicoRepository.existsByCorreo(centro.getCorreo())) {
            throw new RuntimeException("Centro ya existe");
        }
    
        // Guardar en la base de datos
        CentroMedico guardado = centroMedicoRepository.save(centro);
    
        // Crear en Firebase Authentication
        try {
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(guardado.getCorreo())
                .setPassword("KalaTemporal123") // contraseña temporal
                .setEmailVerified(false)
                .setDisabled(false);
    
            FirebaseAuth.getInstance().createUser(request);
        } catch (Exception e) {
            e.printStackTrace();
            // (Opcional) Revertir si falla la creación en Firebase
        }
    
        return guardado;
    }
    
}
