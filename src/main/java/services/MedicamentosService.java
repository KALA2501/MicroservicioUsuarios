package services;
import entities.*;
import repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class MedicamentosService {
    @Autowired
    private MedicamentosRepository repository;

    public List<Medicamentos> obtenerTodos() {
        return repository.findAll();
    }
}