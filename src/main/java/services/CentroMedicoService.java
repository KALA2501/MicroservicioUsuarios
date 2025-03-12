package services;
import entities.*;
import repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;


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
}
