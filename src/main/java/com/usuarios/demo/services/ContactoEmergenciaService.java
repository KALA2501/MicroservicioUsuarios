package com.usuarios.demo.services;

import com.usuarios.demo.entities.*;
import com.usuarios.demo.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ContactoEmergenciaService {
    @Autowired
    private ContactoEmergenciaRepository repository;

    public List<ContactoEmergencia> obtenerTodos() {
        return repository.findAll();
    }

    public Optional<ContactoEmergencia> obtenerPorId(Long id) {
        return repository.findById(id);
    }

    public ContactoEmergencia guardar(ContactoEmergencia contactoEmergencia) {
        return repository.save(contactoEmergencia);
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}
