package com.usuarios.demo.services;

import com.usuarios.demo.entities.*;
import com.usuarios.demo.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TipoVinculacionService {
    @Autowired
    private TipoVinculacionRepository repository;

    public List<TipoVinculacion> obtenerTodos() {
        return repository.findAll();
    }

    public Optional<TipoVinculacion> obtenerPorId(String id) {
        return repository.findById(id);
    }

    public TipoVinculacion guardar(TipoVinculacion tipoVinculacion) {
        return repository.save(tipoVinculacion);
    }

    public void eliminar(String id) {
        repository.deleteById(id);
    }
}
