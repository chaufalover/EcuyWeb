package com.project.ecuy.controller.api;

import com.project.ecuy.services.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * Controlador REST para las operaciones del dashboard.
 * Todas las rutas están bajo el prefijo "/api/dashboard".
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {

    private static final String MODULE_PROGRESS_ERROR = "Error al obtener el progreso del módulo";
    
    private final StatsService statsService;

    @Autowired
    public DashboardApiController(StatsService statsService) {
        this.statsService = statsService;
    }

    /**
     * Obtiene el progreso de los usuarios para un módulo específico.
     * 
     * @param moduloId ID del módulo del cual se desea obtener el progreso
     * @return ResponseEntity con el progreso del módulo o un mensaje de error
     */
    @GetMapping("/progreso-modulo/{moduloId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getProgresoPorModulo(
            @PathVariable("moduloId") Long moduloId) {
        
        if (moduloId == null || moduloId <= 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                "ID de módulo inválido: " + moduloId
            );
        }
        
        try {
            Map<String, Object> progreso = statsService.getUserProgressByModule(moduloId);
            
            if (progreso == null || progreso.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            
            return ResponseEntity.ok(progreso);
            
        } catch (ResponseStatusException rse) {
            throw rse; // Re-lanzar excepciones de estado de respuesta
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                String.format("%s con ID %d: %s", MODULE_PROGRESS_ERROR, moduloId, e.getMessage()),
                e
            );
        }
    }
    
    /**
     * Maneja las excepciones de tipo ResponseStatusException.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        return ResponseEntity
            .status(ex.getStatusCode())
            .body(ex.getReason());
    }
    
    /**
     * Maneja excepciones generales no capturadas por otros manejadores.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Error interno del servidor: " + ex.getMessage());
    }
}
