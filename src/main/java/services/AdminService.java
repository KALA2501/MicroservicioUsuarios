package services;
import entities.*;
import repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AdminService {
    @Autowired
    private AdminRepository repository;

    @Autowired
    private CentroMedicoRepository centroMedicoRepository;

    @Autowired
    private SolicitudCentroMedicoRepository solicitudRepository;

    public List<Admin> obtenerTodos() {
        return repository.findAll();
    }

    public void eliminarUsuarioDeBaseDeDatos(String correo) {
        // Intenta eliminar si existe en centro médico
        if (centroMedicoRepository.existsByCorreo(correo)) {
            centroMedicoRepository.deleteByCorreo(correo);
            System.out.println("✅ Eliminado de base de datos (centro médico)");
        } else {
            System.out.println("⚠️ No se encontró en centro médico. Podrías verificar en otras tablas si aplica.");
        }
    }

    public void actualizarSolicitudAlEliminarUsuario(String correo) {
        // Buscar la solicitud por correo
        Optional<SolicitudCentroMedico> solicitudOpt = solicitudRepository.findByCorreo(correo);
        
        if (solicitudOpt.isPresent()) {
            SolicitudCentroMedico solicitud = solicitudOpt.get();
            // Marcar como no procesada
            solicitud.setProcesado(false);
            solicitudRepository.save(solicitud);
            System.out.println("✅ Solicitud actualizada: marcada como no procesada");
        } else {
            System.out.println("⚠️ No se encontró solicitud con el correo: " + correo);
        }
    }
}