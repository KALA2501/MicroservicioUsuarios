package services;
import entities.*;
import repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PacienteService {
    @Autowired
    private PacienteRepository repository;

    public List<Paciente> obtenerTodos() {
        return repository.findAll();
    }

    public Optional<Paciente> obtenerPorId(String id) {
        return repository.findById(id);
    }

    public Paciente guardar(Paciente paciente) {
        return repository.save(paciente);
    }

    public void eliminar(String id) {
        repository.deleteById(id);
    }
}
