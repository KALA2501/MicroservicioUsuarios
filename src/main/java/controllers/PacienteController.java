package controllers;

import entities.CentroMedico;
import entities.Paciente;
import entities.TipoDocumento;
import entities.Vinculacion;
import entities.Medico;
import entities.TipoVinculacion;
import services.PacienteService;
import repositories.CentroMedicoRepository;
import repositories.TipoDocumentoRepository;
import repositories.MedicoRepository;
import repositories.TipoVinculacionRepository;
import repositories.VinculacionRepository;
import services.JwtService;

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
    @PostMapping
    public ResponseEntity<?> guardar(@RequestBody Map<String, Object> data) {
        try {
            Paciente paciente = new Paciente();
            paciente.setPkId(UUID.randomUUID().toString());

            // Campos obligatorios
            paciente.setNombre(data.get("nombre").toString());
            paciente.setIdDocumento(data.get("idDocumento").toString());
            paciente.setTelefono(data.get("telefono").toString());
            paciente.setEmail(data.get("email").toString());

            // Campos opcionales
            paciente.setApellido(data.getOrDefault("apellido", "").toString());
            paciente.setDireccion(data.getOrDefault("direccion", "").toString());
            paciente.setCodigoCIE(data.getOrDefault("codigoCIE", "").toString());
            paciente.setZona(data.getOrDefault("zona", "").toString());
            paciente.setDistrito(data.getOrDefault("distrito", "").toString());
            paciente.setGenero(data.getOrDefault("genero", "").toString());
            paciente.setUrlImagen(data.getOrDefault("urlImagen", "").toString());

            Object etapaRaw = data.get("etapa");
            int etapa = (etapaRaw instanceof Integer) ? (Integer) etapaRaw : Integer.parseInt(etapaRaw.toString());
            paciente.setEtapa(etapa);

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

            // VALIDACIÓN DE DUPLICADOS + GUARDADO
            Paciente nuevo = service.guardarConValidacion(paciente);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear paciente: " + e.getMessage());
        }
    }

    @Operation(summary = "Actualizar paciente", description = "Actualiza los datos de un paciente existente por ID")
    @ApiResponse(responseCode = "200", description = "Paciente actualizado correctamente")
    @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody Paciente paciente) {
        try {
            Paciente actualizado = service.actualizar(id, paciente);
            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        }
    }

    @Operation(summary = "Eliminar paciente y su vinculación", description = "Elimina el paciente identificado por el ID proporcionado y su vinculación con el médico")
    @ApiResponse(responseCode = "200", description = "Paciente eliminado correctamente")
    @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        try {
            if (vinculacionRepository.findByPaciente_PkId(id).isEmpty()) {
                System.out.println("🟡 No hay vinculaciones, se procede a eliminar paciente directo.");
            } else {
                System.out.println("🔗 Hay vinculaciones, eliminándolas...");
                vinculacionRepository.deleteAllByPaciente_PkId(id);
            }
            service.eliminar(id);
            return ResponseEntity.ok("Paciente eliminado exitosamente");
        } catch (RuntimeException e) {
            // Solo si explícitamente el paciente no existe
            if (e.getMessage().contains("Paciente no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al eliminar paciente: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar paciente: " + e.getMessage());
        }
    }

    @Operation(summary = "Listar pacientes por centro médico", description = "Devuelve todos los pacientes asociados al centro médico dado")
    @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    @GetMapping("/centro-medico/{idCentro}")
    public List<Paciente> obtenerPorCentroMedico(@PathVariable Long idCentro) {
        return service.obtenerPorCentroMedico(idCentro);
    }

    @Operation(summary = "Listar pacientes por médico", description = "Devuelve todos los pacientes asociados a un médico según su tarjeta profesional")
    @ApiResponse(responseCode = "200", description = "Pacientes encontrados")
    @GetMapping("/medico")
    public ResponseEntity<?> obtenerPacientesPorMedico(@RequestParam String tarjetaProfesional) {
        try {
            List<Vinculacion> vinculaciones = service.obtenerPacientesPorTarjetaProfesional(tarjetaProfesional);
            return ResponseEntity.ok(vinculaciones);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al buscar pacientes: " + e.getMessage());
        }
    }

    @Operation(summary = "Buscar pacientes por nombre", description = "Devuelve pacientes cuyos nombres coincidan parcialmente con el valor ingresado")
    @GetMapping("/buscar/nombre")
    public List<Paciente> buscarPorNombre(@RequestParam String nombre) {
        return service.buscarPorNombre(nombre);
    }

    @Operation(summary = "Buscar paciente por tipo y número de identificación", description = "Devuelve un paciente que coincida exactamente con el tipo y número de identificación")
    @ApiResponse(responseCode = "200", description = "Paciente encontrado")
    @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    @GetMapping("/buscar/documento")
    public ResponseEntity<?> buscarPorTipoYNumero(@RequestParam String tipo, @RequestParam String numero) {
        Optional<Paciente> paciente = service.buscarPorTipoYNumero(tipo, numero);

        if (paciente.isPresent()) {
            return ResponseEntity.ok(paciente.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Paciente no encontrado");
        }
    }

    @PostMapping("/registrar-completo")
    @Operation(summary = "Registro completo de paciente", description = "Registra un paciente en Firebase y MySQL, y lo vincula con el médico")
    public ResponseEntity<?> registrarPacienteCompleto(@RequestBody Map<String, Object> data,
            HttpServletRequest request) {
        try {
            // Crear usuario en Firebase Authentication
            String correoPaciente = data.get("email").toString();
            String password = data.get("password").toString();

            UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                    .setEmail(correoPaciente)
                    .setPassword(password)
                    .setEmailVerified(false)
                    .setDisabled(false);

            // Verificar si el correo ya existe en Firebase
            try {
                FirebaseAuth.getInstance().getUserByEmail(correoPaciente);
                return ResponseEntity.status(HttpStatus.CONFLICT).body("El correo ya está registrado");
            } catch (FirebaseAuthException e) {
                if (!e.getAuthErrorCode().name().equals("USER_NOT_FOUND")) {
                    throw e; // Si el error no es USER_NOT_FOUND, relanzar la excepción
                }
            }

            UserRecord userRecord = FirebaseAuth.getInstance().createUser(createRequest);

            // Asignar rol personalizado
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", "paciente");
            FirebaseAuth.getInstance().setCustomUserClaims(userRecord.getUid(), claims);

            // Guardar paciente en base de datos
            Paciente paciente = new Paciente();
            paciente.setPkId(UUID.randomUUID().toString());
            paciente.setNombre(data.get("nombre").toString());
            paciente.setApellido(data.get("apellido").toString());
            paciente.setIdDocumento(data.get("idDocumento").toString());
            paciente.setTelefono(data.get("telefono").toString());
            paciente.setEmail(data.get("email").toString());
            paciente.setDireccion(data.getOrDefault("direccion", "").toString());
            paciente.setCodigoCIE(data.getOrDefault("codigoCIE", "").toString());
            paciente.setZona(data.getOrDefault("zona", "").toString());
            paciente.setDistrito(data.getOrDefault("distrito", "").toString());
            paciente.setGenero(data.getOrDefault("genero", "").toString());
            paciente.setUrlImagen(data.getOrDefault("urlImagen", "").toString());

            Object etapaRaw = data.get("etapa");
            int etapa = (etapaRaw instanceof Integer) ? (Integer) etapaRaw : Integer.parseInt(etapaRaw.toString());
            paciente.setEtapa(etapa);

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

            Paciente guardado = service.guardarConValidacion(paciente);

            // 🔐 Vincular al médico autenticado
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            String correoMedico = jwtService.extractUsername(token);

            Medico medico = medicoRepository.findByCorreo(correoMedico)
                    .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

            Long tipoVinculacionId = Long.parseLong(data.get("tipoVinculacionId").toString());

            TipoVinculacion tipo = tipoVinculacionRepository.findById(tipoVinculacionId)
                    .orElseThrow(() -> new RuntimeException("Tipo de vinculación no encontrado"));

            Vinculacion vinculacion = new Vinculacion();
            vinculacion.setPaciente(guardado);
            vinculacion.setMedico(medico);
            vinculacion.setTipoVinculacion(tipo);
            vinculacion.setFechaVinculado(new Timestamp(System.currentTimeMillis()));

            vinculacionRepository.save(vinculacion);

            return ResponseEntity.status(HttpStatus.CREATED).body(guardado);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al registrar paciente: " + e.getMessage());
        }
    }

    @Operation(summary = "Listar pacientes del médico autenticado", description = "Devuelve todos los pacientes asociados al médico autenticado usando el token JWT")
    @ApiResponse(responseCode = "200", description = "Lista de pacientes obtenida exitosamente")
    @ApiResponse(responseCode = "401", description = "No autorizado")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @GetMapping("/del-medico")
    public ResponseEntity<List<Paciente>> listarPacientesDelMedico(@RequestHeader("Authorization") String token) {
        try {
            // Eliminar el prefijo "Bearer " del token
            String jwt = token.replace("Bearer ", "");

            // Obtener la lista de pacientes usando el servicio
            List<Paciente> pacientes = service.obtenerPacientesDelMedico(jwt);

            return ResponseEntity.ok(pacientes);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/buscar-por-correo")
    public ResponseEntity<?> buscarPorCorreo(@RequestParam String email) {
        try {
            // Buscar el paciente por correo
            Paciente paciente = service.buscarPorCorreo(email);
            if (paciente != null) {
                return ResponseEntity.ok(paciente);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Paciente con el correo " + email + " no encontrado.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al buscar el paciente: " + e.getMessage());
        }
    }

    @Operation(summary = "Obtener médicos vinculados a un paciente", description = "Devuelve la lista de médicos asociados a un paciente dado su ID")
    @ApiResponse(responseCode = "200", description = "Lista de médicos obtenida exitosamente")
    @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    @GetMapping("/{id}/medicos")
    public ResponseEntity<?> obtenerMedicosPorPaciente(@PathVariable String id) {
        try {
            // Buscar las vinculaciones del paciente
            List<Vinculacion> vinculaciones = vinculacionRepository.findByPaciente_PkId(id);

            if (vinculaciones.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El paciente no tiene médicos vinculados.");
            }

            // Obtener los médicos de las vinculaciones
            List<Medico> medicos = vinculaciones.stream()
                    .map(Vinculacion::getMedico)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(medicos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener los médicos: " + e.getMessage());
        }
    }
}
