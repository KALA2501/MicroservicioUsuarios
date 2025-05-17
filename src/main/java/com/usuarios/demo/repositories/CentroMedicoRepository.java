package com.usuarios.demo.repositories;

import com.usuarios.demo.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
public interface CentroMedicoRepository extends JpaRepository<CentroMedico, Long> {
    boolean existsByCorreo(String correo);

    Optional<CentroMedico> findByCorreo(String correo);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM centro_medico WHERE correo = ?1", nativeQuery = true)
    void deleteByCorreo(String correo);


}