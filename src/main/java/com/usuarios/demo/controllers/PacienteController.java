package com.usuarios.demo.controllers;

import com.usuarios.demo.entities.*;
import com.usuarios.demo.services.*;
import com.usuarios.demo.repositories.*;
import com.usuarios.demo.services.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.FirebaseAuthException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

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
    private JwtService jwtService;

    @Autowired
    private ContactoEmergenciaRepository contactoEmergenciaRepository;

    @Operation(summary = "Obtener todos los pacientes", description = "Retorna una lista de todos los pacientes registrados en el sistema")
    @ApiResponse(responseCode = "200", description = "Lista de pacientes obtenida exitosamente")
    @GetMapping
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

    @Operation(summary = "Guardar un nuevo paciente", description = "Crea y almacena un nuevo paciente a partir de los datos proporcionados")
    @ApiResponse(responseCode = "201", description = "Paciente guardado correctamente")
    @PostMapping("/crear")
    public ResponseEntity<?> guardar(@RequestBody Map<String, Object> data) {
        try {
            // Validar si ya existe un paciente con el mismo correo
            try {
                service.buscarPorCorreo(data.get("email").toString());
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Ya existe un paciente registrado con este correo electrónico.");
            } catch (RuntimeException e) {
                // Si lanza excepción, significa que NO existe y se puede continuar
            }

            // Validar si ya existe un contacto de emergencia con el mismo teléfono
            Map<String, Object> contactoMap = (Map<String, Object>) data.get("contactoEmergencia");
            String telefonoContacto = contactoMap.get("telefono").toString();
            Optional<ContactoEmergencia> contactoExistente = contactoEmergenciaRepository.findByTelefono(telefonoContacto);
            if (contactoExistente.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Ya existe un contacto de emergencia registrado con este teléfono.");
            }

            Paciente paciente = new Paciente();
            paciente.setPkId(UUID.randomUUID().toString());

            // Recuperar contactoEmergencia por ID del JSON
            Long contactoId = Long.valueOf(contactoMap.get("pkId").toString());
            ContactoEmergencia contacto = contactoEmergenciaRepository.findById(contactoId)
                .orElseThrow(() -> new RuntimeException("Contacto de emergencia no encontrado"));
            paciente.setContactoEmergencia(contacto);

            // Otros campos permanecen igual
            paciente.setNombre(data.get("nombre").toString());
            paciente.setApellido(data.getOrDefault("apellido", "").toString());
            paciente.setIdDocumento(data.get("idDocumento").toString());
            paciente.setTelefono(data.get("telefono").toString());
            paciente.setEmail(data.get("email").toString());
            paciente.setDireccion(data.getOrDefault("direccion", "").toString());
            paciente.setGenero(data.getOrDefault("genero", "").toString());
            paciente.setUrlImagen(data.getOrDefault("urlImagen", "").toString());

            Object etapaRaw = data.get("etapa");
            int etapa = (etapaRaw instanceof Integer) ? (Integer) etapaRaw : Integer.parseInt(etapaRaw.toString());
            paciente.setEtapa(etapa);

            Object codigoCieRaw = data.get("codigoCIE");
            int codigo_cie = (codigoCieRaw instanceof Integer) ? (Integer) codigoCieRaw : Integer.parseInt(codigoCieRaw.toString());
            paciente.setCodigoCIE(codigo_cie);

            String fechaNacStr = data.get("fechaNacimiento").toString();
            paciente.setFechaNacimiento(Timestamp.valueOf(fechaNacStr + " 00:00:00"));

            Long idCentro = Long.parseLong(data.get("centroMedico").toString());
            CentroMedico centro = centroMedicoRepository.findById(idCentro)
                    .orElseThrow(() -> new RuntimeException("Centro médico no encontrado"));
            paciente.setCentroMedico(centro);

            String idTipoDoc = data.get("tipoDocumento").toString();
            TipoDocumento tipoDoc = tipoDocumentoRepository.findById(idTipoDoc)
                    .orElseThrow(() -> new RuntimeException("Tipo de documento no encontrado"));
            paciente.setTipoDocumento(tipoDoc);

            // Crear usuario en Firebase Authentication
            UserRecord userRecord;
            try {
                userRecord = FirebaseAuth.getInstance().getUserByEmail(paciente.getEmail());
            } catch (Exception e) {
                UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                        .setEmail(paciente.getEmail())
                        .setPassword("paciente123") // contraseña temporal
                        .setEmailVerified(false)
                        .setDisabled(false);
                userRecord = FirebaseAuth.getInstance().createUser(request);
            }

            // Asignar custom claim (rol: paciente)
            Map<String, Object> claims = new HashMap<>();
            claims.put("rol", "paciente");
            FirebaseAuth.getInstance().setCustomUserClaims(userRecord.getUid(), claims);

            // VALIDACIÓN DE DUPLICADOS + GUARDADO
            Paciente nuevo = service.guardarConValidacion(paciente);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar paciente: " + e.getMessage());
        }
    }

    @GetMapping("/centro-medico/{id}")
    @Operation(summary = "Obtener pacientes por centro médico", description = "Retorna los pacientes asociados a un centro médico")
    public ResponseEntity<?> obtenerPacientesPorCentro(@PathVariable Long id) {
        try {
            List<Paciente> pacientes = service.obtenerPorCentroMedico(id);
            return ResponseEntity.ok(pacientes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener pacientes: " + e.getMessage());
        }
    }

    @PostMapping("/registrar-completo")
    @Operation(summary = "Registrar paciente con lógica extendida", description = "Registra un paciente completo con contacto de emergencia existente o nuevo, y lo vincula al médico")
    @ApiResponse(responseCode = "201", description = "Paciente creado exitosamente")
    public ResponseEntity<?> registrarPacienteCompleto(@RequestBody Map<String, Object> data) {
        try {
            // Verificar si ya existe un paciente con el correo
            try {
                service.buscarPorCorreo(data.get("email").toString());
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Ya existe un paciente con ese correo.");
            } catch (RuntimeException e) {
                // No existe, continúa
            }

            // Paso 1: Recuperar o validar contacto de emergencia por ID
            Map<String, Object> contactoMap = (Map<String, Object>) data.get("contactoEmergencia");
            Long contactoId = Long.valueOf(contactoMap.get("pkId").toString());
            ContactoEmergencia contacto = contactoEmergenciaRepository.findById(contactoId)
                .orElseThrow(() -> new RuntimeException("Contacto de emergencia no encontrado con ID: " + contactoId));

            // Paso 2: Armar entidad Paciente
            Paciente paciente = new Paciente();
            paciente.setPkId(UUID.randomUUID().toString());
            paciente.setContactoEmergencia(contacto);
            paciente.setNombre(data.get("nombre").toString());
            paciente.setApellido(data.getOrDefault("apellido", "").toString());
            paciente.setIdDocumento(data.get("idDocumento").toString());
            paciente.setTelefono(data.get("telefono").toString());
            paciente.setEmail(data.get("email").toString());
            paciente.setDireccion(data.getOrDefault("direccion", "").toString());
            paciente.setGenero(data.getOrDefault("genero", "").toString());
            paciente.setUrlImagen(data.getOrDefault("urlImagen", "").toString());

            Object etapaRaw = data.get("etapa");
            int etapa = (etapaRaw instanceof Integer) ? (Integer) etapaRaw : Integer.parseInt(etapaRaw.toString());
            paciente.setEtapa(etapa);

            Object codigoCieRaw = data.get("codigoCIE");
            int codigo_cie = (codigoCieRaw instanceof Integer) ? (Integer) codigoCieRaw : Integer.parseInt(codigoCieRaw.toString());
            paciente.setCodigoCIE(codigo_cie);

            String fechaNacStr = data.get("fechaNacimiento").toString();
            paciente.setFechaNacimiento(Timestamp.valueOf(fechaNacStr + " 00:00:00"));

            // Centro médico
            Long idCentro = Long.parseLong(data.get("centroMedico").toString());
            CentroMedico centro = centroMedicoRepository.findById(idCentro)
                .orElseThrow(() -> new RuntimeException("Centro médico no encontrado"));
            paciente.setCentroMedico(centro);

            // Tipo de documento
            String idTipoDoc = data.get("tipoDocumento").toString();
            TipoDocumento tipoDoc = tipoDocumentoRepository.findById(idTipoDoc)
                .orElseThrow(() -> new RuntimeException("Tipo de documento no encontrado"));
            paciente.setTipoDocumento(tipoDoc);

            // Crear usuario en Firebase Authentication (si no existe)
            UserRecord userRecord;
            try {
                userRecord = FirebaseAuth.getInstance().getUserByEmail(paciente.getEmail());
            } catch (Exception e) {
                UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(paciente.getEmail())
                    .setPassword("paciente123") // Contraseña temporal
                    .setEmailVerified(false)
                    .setDisabled(false);
                userRecord = FirebaseAuth.getInstance().createUser(request);
            }

            // Asignar custom claim (rol: paciente)
            Map<String, Object> claims = new HashMap<>();
            claims.put("rol", "paciente");
            FirebaseAuth.getInstance().setCustomUserClaims(userRecord.getUid(), claims);

            // Guardar paciente
            Paciente nuevo = service.guardarConValidacion(paciente);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error al registrar paciente: " + e.getMessage());
        }
    }


}
