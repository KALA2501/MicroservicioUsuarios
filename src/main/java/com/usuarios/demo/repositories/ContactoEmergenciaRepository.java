package com.usuarios.demo.repositories;

import com.usuarios.demo.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ContactoEmergenciaRepository extends JpaRepository<ContactoEmergencia, Long> {
    Optional<ContactoEmergencia> findByTelefono(String telefono);
}
