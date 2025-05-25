package com.usuarios.demo.controllers;

import com.usuarios.demo.entities.*;
import com.usuarios.demo.repositories.*;
import com.usuarios.demo.services.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.security.Principal;

@RestController
@RequestMapping("/api/pacientes")
@Tag(name = "Pacientes", description = "Endpoints para la gestión de pacientes")
public class PacienteController {

    @Autowired
    private PacienteService service;

    @Autowired
    private CentroMedicoRepository centroMedicoRepository;

    @Autowired
    private TipoDocumentoRepository tipoDocumentoRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private TipoVinculacionRepository tipoVinculacionRepository;

    @Autowired
    private VinculacionRepository vinculacionRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ContactoEmergenciaRepository contactoEmergenciaRepository;

    @Autowired
    private FirebaseAuth firebaseAuth;

    @GetMapping("/mi-perfil")
    public ResponseEntity<?> obtenerMiPerfil(Principal principal) {
        String email = principal.getName();
        Paciente paciente = service.buscarPorCorreo(email);
        return ResponseEntity.ok(paciente.getPkId());
    }

    @GetMapping
    @Operation(summary = "Obtener todos los pacientes", description = "Retorna una lista de todos los pacientes registrados en el sistema")
    @ApiResponse(responseCode = "200", description = "Lista de pacientes obtenida exitosamente")
    public List<Paciente> obtenerTodos() {
        return service.obtenerTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener paciente por ID", description = "Retorna el paciente que coincide con el ID proporcionado")
    @ApiResponse(responseCode = "200", description = "Paciente encontrado")
    @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    public ResponseEntity<?> obtenerPorId(@PathVariable String id) {
        Optional<Paciente> paciente = service.obtenerPorId(id);
        if (paciente.isPresent()) {
            return ResponseEntity.ok(paciente.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Paciente no encontrado");
        }
    }

    @PostMapping("/crear")
    @Operation(summary = "Guardar un nuevo paciente", description = "Crea y almacena un nuevo paciente a partir de los datos proporcionados")
    @ApiResponse(responseCode = "201", description = "Paciente guardado correctamente")
    public ResponseEntity<?> guardar(@RequestBody Map<String, Object> data) {
        try {
            if (service.buscarPorCorreo(data.get("email").toString()) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Ya existe un paciente con este correo.");
            }
        } catch (RuntimeException ignored) {}

        String telefono = getMap(data, "contactoEmergencia").get("telefono").toString();
        if (contactoEmergenciaRepository.findByTelefono(telefono).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Ya existe un contacto de emergencia con este teléfono.");
        }

        try {
            Paciente paciente = buildPacienteFromData(data);
            createFirebaseUserIfNotExists(paciente);
            Paciente nuevo = service.guardarConValidacion(paciente);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar paciente: " + e.getMessage());
        }
    }

    @PostMapping("/registrar-completo")
    @Operation(summary = "Registrar paciente con lógica extendida", description = "Registra un paciente completo y lo vincula al médico")
    @ApiResponse(responseCode = "201", description = "Paciente creado exitosamente")
    public ResponseEntity<?> registrarPacienteCompleto(@RequestBody Map<String, Object> data) {
        try {
            if (service.buscarPorCorreo(data.get("email").toString()) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Ya existe un paciente con este correo.");
            }
        } catch (RuntimeException ignored) {}

        try {
            Medico medico = medicoRepository.findById(getMap(data, "medico").get("pkId").toString())
                    .orElseThrow(() -> new RuntimeException("Médico no encontrado"));
            TipoVinculacion tipoVinculacion = tipoVinculacionRepository.findById(getMap(data, "tipoVinculacion").get("id").toString())
                    .orElseThrow(() -> new RuntimeException("Tipo de vinculación no encontrado"));

            Paciente paciente = buildPacienteFromData(data);
            createFirebaseUserIfNotExists(paciente);
            Paciente nuevo = service.guardarConValidacion(paciente, medico, tipoVinculacion);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error al registrar paciente: " + e.getMessage());
        }
    }

    @GetMapping("/centro-medico/{id}")
    @Operation(summary = "Obtener pacientes por centro médico", description = "Retorna los pacientes asociados a un centro médico")
public List<Map<String, Object>> obtenerPacientesConMedicos(@PathVariable Long id) {
    List<Paciente> pacientes = pacienteRepository.findByCentroMedico_PkId(id);
    return pacientes.stream().map(p -> {
        Map<String, Object> map = new HashMap<>();
        map.put("nombre", p.getNombre());
        map.put("apellido", p.getApellido());
        map.put("idDocumento", p.getIdDocumento());
        map.put("telefono", p.getTelefono());
        
        List<Map<String, String>> medicos = vinculacionRepository.findByPaciente(p).stream()
            .map(v -> {
                Medico m = v.getMedico();
                Map<String, String> medicoMap = new HashMap<>();
                medicoMap.put("nombre", m.getNombre());
                medicoMap.put("apellido", m.getApellido());
                return medicoMap;
            }).collect(Collectors.toList());

        map.put("medicos", medicos);
        return map;
    }).toList();
}


    @GetMapping("/{id}/medicos")
    @Operation(summary = "Obtener médicos vinculados a un paciente", description = "Incluye nombre, especialidad y fecha de vinculación")
    public ResponseEntity<?> obtenerMedicosVinculados(@PathVariable String id) {
        try {
            List<Vinculacion> vinculaciones = vinculacionRepository.findByPaciente_PkId(id);
            if (vinculaciones.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El paciente no tiene médicos vinculados.");
            }

            List<Map<String, Object>> medicos = vinculaciones.stream().map(v -> {
                Map<String, Object> info = new HashMap<>();
                info.put("nombre", v.getMedico().getNombre());
                info.put("especialidad", v.getMedico().getEspecialidad());
                info.put("fechaVinculacion", v.getFechaVinculado());
                return info;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(medicos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener médicos vinculados: " + e.getMessage());
        }
    }

    @GetMapping("/del-medico")
    public ResponseEntity<?> obtenerPacientesDelMedico(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            return ResponseEntity.ok(service.obtenerPacientesDelMedico(token));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno: " + e.getMessage());
        }
    }

    // ----------- Utility Methods -----------

    private Map<String, Object> getMap(Map<String, Object> data, String key) {
        Object raw = data.get(key);
        if (raw instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> safeMap = (Map<String, Object>) map;
            return safeMap;
        }
        throw new IllegalArgumentException(key + " no es un objeto válido.");
    }

    private Paciente buildPacienteFromData(Map<String, Object> data) {
        Paciente paciente = new Paciente();
        paciente.setPkId(UUID.randomUUID().toString());

        Map<String, Object> contactoMap = getMap(data, "contactoEmergencia");
        Long contactoId = Long.valueOf(contactoMap.get("pkId").toString());
        ContactoEmergencia contacto = contactoEmergenciaRepository.findById(contactoId)
                .orElseThrow(() -> new RuntimeException("Contacto de emergencia no encontrado"));
        paciente.setContactoEmergencia(contacto);

        paciente.setNombre(data.get("nombre").toString());
        paciente.setApellido(data.getOrDefault("apellido", "").toString());
        paciente.setIdDocumento(data.get("idDocumento").toString());
        paciente.setTelefono(data.get("telefono").toString());
        paciente.setEmail(data.get("email").toString());
        paciente.setDireccion(data.getOrDefault("direccion", "").toString());
        paciente.setGenero(data.getOrDefault("genero", "").toString());
        paciente.setUrlImagen(data.getOrDefault("urlImagen", "").toString());
        paciente.setEtapa(Integer.parseInt(data.get("etapa").toString()));
        paciente.setCodigoCIE(Integer.parseInt(data.get("codigoCIE").toString()));
        paciente.setFechaNacimiento(Timestamp.valueOf(data.get("fechaNacimiento") + " 00:00:00"));

        Map<String, Object> centroMap = getMap(data, "centroMedico");
        Long idCentro = Long.valueOf(centroMap.get("pkId").toString());
        CentroMedico centro = centroMedicoRepository.findById(idCentro)
                .orElseThrow(() -> new RuntimeException("Centro médico no encontrado"));
        paciente.setCentroMedico(centro);

        Map<String, Object> tipoDocMap = getMap(data, "tipoDocumento");
        String idTipoDoc = tipoDocMap.get("id").toString();
        TipoDocumento tipoDoc = tipoDocumentoRepository.findById(idTipoDoc)
                .orElseThrow(() -> new RuntimeException("Tipo de documento no encontrado"));
        paciente.setTipoDocumento(tipoDoc);

        return paciente;
    }

    private void createFirebaseUserIfNotExists(Paciente paciente) throws Exception {
        UserRecord userRecord;
        try {
            userRecord = firebaseAuth.getUserByEmail(paciente.getEmail());
        } catch (Exception e) {
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(paciente.getEmail())
                    .setPassword("paciente123")
                    .setEmailVerified(false)
                    .setDisabled(false);
            userRecord = firebaseAuth.createUser(request);
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", "paciente");
        firebaseAuth.setCustomUserClaims(userRecord.getUid(), claims);
    }
}
