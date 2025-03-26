package services;

import entities.SolicitudCentroMedico;
import repositories.SolicitudCentroMedicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
