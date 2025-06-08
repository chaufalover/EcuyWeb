package com.project.ecuy.controller;

import com.project.ecuy.entities.Activity;
import com.project.ecuy.exception.ForbiddenAccessException;
import com.project.ecuy.exception.ResourceNotFoundException;
import com.project.ecuy.services.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/puzzle")
public class PuzzleController {
    private static final Logger logger = LoggerFactory.getLogger(PuzzleController.class);

    private final ActivityService activityService;

    @Autowired
    public PuzzleController(ActivityService activityService) {
        this.activityService = activityService;
    }

    /**
     * 
     * @param activityId 
     * @return 
     */
    /**
     * 
     * @param activityId 
     * @return 
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/activity/{activityId}")
    public ResponseEntity<?> getPuzzleConfig(@PathVariable Long activityId) {
        try {
            if (!hasAccessToActivity(activityId)) {
                throw new ForbiddenAccessException("No tienes permiso para acceder a esta actividad");
            }
            
            Activity activity = activityService.getPuzzleConfig(activityId);
            
            if (activity.getUrlImagenRompecabezas() == null || activity.getUrlImagenRompecabezas().isEmpty()) {
                throw new ResourceNotFoundException("No se encontró la imagen del rompecabezas");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("imageUrl", activity.getUrlImagenRompecabezas());
            response.put("filas", 3);
            response.put("columnas", 3);
            
            return ResponseEntity.ok(response);
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("status", "error", "message", e.getMessage()));
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", "Error al cargar la configuración del rompecabezas"));
        }
    }

    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/activity/{activityId}/solution")
    public ResponseEntity<?> getPuzzleSolution(@PathVariable Long activityId) {
        try {
            if (!hasAccessToActivity(activityId)) {
                throw new ForbiddenAccessException("No tienes permiso para acceder a esta actividad");
            }
            
            List<Integer> solucion = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "solution", solucion
            ));
            
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", "Error al cargar la solución del rompecabezas"));
        }
    }

    /**
     * 
     * @param activityId 
     * @param config 
     * @return 
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/activity/{activityId}")
    public ResponseEntity<?> updatePuzzleConfig(
            @PathVariable Long activityId,
            @RequestBody Map<String, Object> config) {
        try {
            String imageUrl = (String) config.get("imageUrl");
            
            if (imageUrl == null || imageUrl.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "La URL de la imagen es requerida"));
            }
            
            Activity updatedActivity = activityService.updatePuzzleConfig(activityId, imageUrl);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Configuración del rompecabezas actualizada correctamente",
                "data", Map.of(
                    "imageUrl", updatedActivity.getUrlImagenRompecabezas(),
                    "filas", 3,
                    "columnas", 3
                )
            ));
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", "Error al actualizar la configuración del rompecabezas: " + e.getMessage()));
        }
    }

    /**
     * 
     * @param activityId 
     * @param currentPositions 
     * @return 
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/activity/{activityId}/check")
    public ResponseEntity<?> checkPuzzleSolution(
            @PathVariable Long activityId,
            @RequestBody Map<String, Object> request) {
        try {
            if (!hasAccessToActivity(activityId)) {
                throw new ForbiddenAccessException("No tienes permiso para acceder a esta actividad");
            }
            
            @SuppressWarnings("unchecked")
            List<Integer> currentPositions = (List<Integer>) request.get("currentPositions");
            
            if (currentPositions == null || currentPositions.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "Se requieren las posiciones actuales del rompecabezas"));
            }
            
            
            int totalPiezas = 9;
            
            
            if (currentPositions.size() != totalPiezas) {
                return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "El rompecabezas debe tener exactamente 9 piezas (3x3)"));
            }
            
            
            boolean isCorrect = true;
            for (int i = 0; i < currentPositions.size(); i++) {
                if (currentPositions.get(i) != i) {
                    isCorrect = false;
                    break;
                }
            }
            
            
            if (isCorrect) {
                try {
                    activityService.marcarComoCompletada(activityId, true);
                } catch (Exception e) {
                    logger.error("Error al marcar la actividad como completada: {}", e.getMessage(), e);
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "isCorrect", isCorrect,
                "message", isCorrect ? "¡Felicidades! Has completado el rompecabezas correctamente." : "La solución no es correcta. Sigue intentándolo."
            ));
            
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", "Error al verificar la solución del rompecabezas: " + e.getMessage()));
        }
    }

    private boolean hasAccessToActivity(Long activityId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        return true; 
    }
}
