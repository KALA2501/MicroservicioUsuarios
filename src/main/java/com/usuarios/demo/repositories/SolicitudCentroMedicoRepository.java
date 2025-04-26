package com.usuarios.demo.repositories;

import com.usuarios.demo.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SolicitudCentroMedicoRepository extends JpaRepository<SolicitudCentroMedico, Long> {
    boolean existsByCorreo(String correo);

    boolean existsByTelefono(String telefono);

    Optional<SolicitudCentroMedico> findByCorreo(String correo);
}
