package com.usuarios.demo.services;

import com.usuarios.demo.entities.*;
import com.usuarios.demo.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import java.util.Optional;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Service
public class MedicoService {
    @Autowired
    private MedicoRepository repository;

    @Autowired
    private CentroMedicoRepository centroMedicoRepository;

    @Autowired
    private TipoDocumentoRepository tipoDocumentoRepository;

    public Optional<CentroMedico> obtenerCentroPorId(Long id) {
        return centroMedicoRepository.findById(id);
    }

    public Optional<TipoDocumento> obtenerTipoDocumentoPorId(String id) {
        return tipoDocumentoRepository.findById(id);
    }

    public List<Medico> obtenerTodos() {
        return repository.findAll();
    }

    public Optional<Medico> obtenerPorId(String id) {
        return repository.findById(id);
    }

    public Medico guardar(Medico medico) {
        if (medico.getPkId() == null || medico.getPkId().isBlank()) {
            medico.setPkId(UUID.randomUUID().toString());
        }

        if (medico.getCentroMedico() != null && medico.getCentroMedico().getPkId() != null) {
            CentroMedico centro = centroMedicoRepository.findById(medico.getCentroMedico().getPkId())
                    .orElseThrow(() -> new RuntimeException("Centro médico no encontrado"));
            medico.setCentroMedico(centro);
        }

        if (medico.getTipoDocumento() != null && medico.getTipoDocumento().getId() != null) {
            TipoDocumento tipoDoc = tipoDocumentoRepository.findById(medico.getTipoDocumento().getId())
                    .orElseThrow(() -> new RuntimeException("Tipo de documento no encontrado"));
            medico.setTipoDocumento(tipoDoc);
        }

        return repository.save(medico);
    }

    public void eliminar(String id) {
        Optional<Medico> medicoOpt = repository.findById(id);

        if (medicoOpt.isEmpty()) {
            throw new RuntimeException("Médico no encontrado");
        }

        Medico medico = medicoOpt.get();

        // 1. Eliminar en Firebase Authentication
        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(medico.getCorreo());
            FirebaseAuth.getInstance().deleteUser(userRecord.getUid());
            System.out.println("✅ Usuario de Firebase eliminado: " + medico.getCorreo());
        } catch (FirebaseAuthException e) {
            System.err.println("❌ Error al eliminar usuario en Firebase: " + e.getMessage());
            // opcional: continuar con la eliminación local incluso si falla Firebase
        }

        // 2. Eliminar en base de datos
        repository.deleteById(id);
        System.out.println("✅ Médico eliminado de la base de datos con ID: " + id);
    }

    public List<Medico> obtenerPorCentroMedico(Long idCentro) {
        return repository.findByCentroMedicoPkId(idCentro);
    }

    public List<Medico> filtrarMedicos(String nombre, String tarjeta, String profesion) {
        if (nombre != null && !nombre.isEmpty()) {
            return repository.findByNombreContainingIgnoreCase(nombre);
        } else if (tarjeta != null && !tarjeta.isEmpty()) {
            return repository.findByTarjetaProfesionalContainingIgnoreCase(tarjeta);
        } else if (profesion != null && !profesion.isEmpty()) {
            return repository.findByProfesionContainingIgnoreCase(profesion);
        }
        return repository.findAll();
    }

    public Optional<Medico> obtenerPorCorreo(String correo) {
        return repository.findByCorreo(correo);
    }

}
