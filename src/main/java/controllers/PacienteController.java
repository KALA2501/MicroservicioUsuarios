package controllers;

import entities.CentroMedico;
import entities.Paciente;
import entities.TipoDocumento;
import entities.Vinculacion;
import services.PacienteService;
import repositories.CentroMedicoRepository;
import repositories.TipoDocumentoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.sql.Timestamp;
import java.util.*;

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

    @Operation(summary = "Obtener todos los pacientes", description = "Retorna una lista de todos los pacientes registrados en el sistema")
    @ApiResponse(responseCode = "200", description = "Lista de pacientes obtenida exitosamente")
    @GetMapping
    public List<Paciente> obtenerTodos() {
        return service.obtenerTodos();
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener paciente por ID",
        description = "Retorna el paciente que coincide con el ID proporcionado"
    )
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
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear paciente: " + e.getMessage());
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

    @Operation(summary = "Eliminar paciente", description = "Elimina el paciente identificado por el ID proporcionado")
    @ApiResponse(responseCode = "200", description = "Paciente eliminado correctamente")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        try {
            service.eliminar(id);
            return ResponseEntity.ok("Paciente eliminado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error al eliminar: " + e.getMessage());
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al buscar pacientes: " + e.getMessage());
        }
    }

    @Operation(summary = "Buscar pacientes por nombre", description = "Devuelve pacientes cuyos nombres coincidan parcialmente con el valor ingresado")
    @GetMapping("/buscar/nombre")
    public List<Paciente> buscarPorNombre(@RequestParam String nombre) {
        return service.buscarPorNombre(nombre);
    }
    @Operation(
        summary = "Buscar paciente por tipo y número de identificación",
        description = "Devuelve un paciente que coincida exactamente con el tipo y número de identificación"
    )
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

    @Operation(summary = "Filtrar pacientes por etapa de enfermedad")
    @GetMapping("/etapa")
    public ResponseEntity<List<Paciente>> filtrarPorEtapa(@RequestParam Integer etapa) {
        List<Paciente> resultados = service.filtrarPorEtapa(etapa);
        return ResponseEntity.ok(resultados);
    }
    
}
