package com.usuarios.demo.services;

import com.usuarios.demo.entities.*;
import com.usuarios.demo.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ComentarioService {
    @Autowired
    private ComentarioRepository repository;

    public List<Comentario> obtenerTodos() {
        return repository.findAll();
    }

    public Optional<Comentario> obtenerPorId(Long id) {
        return repository.findById(id);
    }

    public Comentario guardar(Comentario comentario) {
        return repository.save(comentario);
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}
