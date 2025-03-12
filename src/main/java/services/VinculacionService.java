package services;
import entities.*;
import repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class VinculacionService {
    @Autowired
    private VinculacionRepository repository;

    public List<Vinculacion> obtenerTodos() {
        return repository.findAll();
    }
}
