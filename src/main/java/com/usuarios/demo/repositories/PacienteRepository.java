package com.usuarios.demo.repositories;

import com.usuarios.demo.entities.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, String> {

    List<Paciente> findByCentroMedicoPkId(Long idCentro);

    List<Paciente> findByNombreContainingIgnoreCase(String nombre);

    Optional<Paciente> findByTipoDocumento_IdAndIdDocumento(String tipoDocumentoId, String idDocumento);

    boolean existsByTelefono(String telefono);

    Optional<Paciente> findByEmail(String email);

    // Buscar por nombre o apellido ignorando mayúsculas
    List<Paciente> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(String nombre, String apellido);

    // Buscar por etapa de enfermedad
    List<Paciente> findByEtapa(Integer etapa);

    // Buscar pacientes directamente por su clave primaria (pkId)
    Optional<Paciente> findByPkId(String pkId);

}
