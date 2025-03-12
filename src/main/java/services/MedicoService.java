package services;
import entities.*;
import repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class MedicoService {
    @Autowired
    private MedicoRepository repository;

    public List<Medico> obtenerTodos() {
        return repository.findAll();
    }

    public Optional<Medico> obtenerPorId(String id) {
        return repository.findById(id);
    }

    public Medico guardar(Medico medico) {
        return repository.save(medico);
    }

    public void eliminar(String id) {
        repository.deleteById(id);
    }
}
