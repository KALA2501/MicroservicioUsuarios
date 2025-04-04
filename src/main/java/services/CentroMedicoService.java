package services;
import entities.*;
import repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import java.util.HashMap;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;



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
            centro.setUrlLogo(nuevosDatos.getUrlLogo());
            return repository.save(centro);
        } else {
            throw new RuntimeException("Centro médico no encontrado con ID: " + id);
        }
    }

    @Autowired
    private CentroMedicoRepository centroMedicoRepository;
    public CentroMedico registrarCentroMedico(CentroMedico centro) {
        // Validar campos obligatorios
        if (centro.getNombre() == null || centro.getCorreo() == null || centro.getTelefono() == null) {
            throw new RuntimeException("Faltan datos obligatorios");
        }

        // Verificar si el correo ya existe
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
    
            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
            
            Map<String, Object> claims = new HashMap<>();
            claims.put("rol", "centro_medico");
            FirebaseAuth.getInstance().setCustomUserClaims(userRecord.getUid(), claims);
            
            System.out.println("✅ Usuario creado y rol asignado: centro_medico");
        } catch (Exception e) {
            e.printStackTrace();
            // (Opcional) Revertir si falla la creación en Firebase
        }
    
        return guardado;
    }
    
    @Transactional
    public void eliminarPorCorreo(String correo) {
        try {
            // Primero intentamos encontrar el centro médico
            Optional<CentroMedico> centro = centroMedicoRepository.findByCorreo(correo);
            
            if (centro.isPresent()) {
                // Si existe, lo eliminamos usando el método delete
                centroMedicoRepository.delete(centro.get());
                System.out.println("✅ Centro médico eliminado correctamente: " + correo);
            } else {
                System.out.println("⚠️ No se encontró centro médico con el correo: " + correo);
            }
        } catch (Exception e) {
            System.err.println("❌ Error al eliminar centro médico: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar centro médico con correo: " + correo, e);
        }
    }

    public boolean existePorCorreo(String correo) {
        return centroMedicoRepository.existsByCorreo(correo);
    }
}
