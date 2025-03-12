package services;
import entities.*;
import repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TipoDocumentoService {
    @Autowired
    private TipoDocumentoRepository repository;

    public List<TipoDocumento> obtenerTodos() {
        return repository.findAll();
    }

    public Optional<TipoDocumento> obtenerPorId(String id) {
        return repository.findById(id);
    }

    public TipoDocumento guardar(TipoDocumento tipoDocumento) {
        return repository.save(tipoDocumento);
    }

    public void eliminar(String id) {
        repository.deleteById(id);
    }
}

