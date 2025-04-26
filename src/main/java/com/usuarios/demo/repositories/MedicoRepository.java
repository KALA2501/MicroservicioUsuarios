package com.usuarios.demo.repositories;

import com.usuarios.demo.entities.*;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, String> {
    List<Medico> findByCentroMedicoPkId(Long pkIdCentro);

    List<Medico> findByNombreContainingIgnoreCase(String nombre);

    List<Medico> findByTarjetaProfesionalContainingIgnoreCase(String tarjetaProfesional);

    List<Medico> findByProfesionContainingIgnoreCase(String profesion);

    Optional<Medico> findByCorreo(String correo);

}
