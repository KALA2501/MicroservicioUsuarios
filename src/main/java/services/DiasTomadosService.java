package services;
import entities.*;
import repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class DiasTomadosService {
    @Autowired
    private DiasTomadosRepository repository;

    public List<DiasTomados> obtenerTodos() {
        return repository.findAll();
    }
}
