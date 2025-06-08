package com.project.ecuy.controller;

import com.project.ecuy.entities.Activity;
import com.project.ecuy.services.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/actividad")
public class ActivityApiController {

    private static final Logger logger = LoggerFactory.getLogger(ActivityApiController.class);
    private final ActivityService activityService;

    @Autowired
    public ActivityApiController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping("/siguiente/{id}")
    public ResponseEntity<?> obtenerSiguienteActividad(
            @PathVariable("id") String idStr,
            @RequestParam(name = "moduloId", required = false) Long moduloId) {
        
        logger.info("=== SOLICITUD PARA OBTENER SIGUIENTE ACTIVIDAD ===");
        logger.info("ID de actividad recibido como string: {}", idStr);
        logger.info("ID de módulo: {}", moduloId);
        
        
        if (idStr == null || idStr.trim().isEmpty() || "undefined".equalsIgnoreCase(idStr)) {
            logger.error("ID de actividad no proporcionado o inválido: {}", idStr);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "/modulos")
                    .build();
        }
        
        
        Long id;
        try {
            id = Long.parseLong(idStr);
            logger.info("ID de actividad convertido a Long: {}", id);
        } catch (NumberFormatException e) {
            logger.error("Error al convertir el ID de actividad a número: {}", idStr);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "/modulos")
                    .build();
        }
        
        try {
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
                logger.error("Usuario no autenticado");
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", "/login")
                        .build();
            }
            
            try {
                List<Activity> siguientes = activityService.obtenerSiguientesActividades(id);
                
                if (siguientes != null && !siguientes.isEmpty()) {
                    logger.info("Redirigiendo a la siguiente actividad: ID {}", siguientes.get(0).getId());
                    return ResponseEntity.status(HttpStatus.FOUND)
                            .header("Location", "/actividad/" + siguientes.get(0).getId() + 
                                    (moduloId != null ? "?moduloId=" + moduloId : ""))
                            .build();
                } else {
                    logger.info("No hay más actividades siguientes, redirigiendo a módulos");
                    String redirectUrl = "/modulos";
                    if (moduloId != null) {
                        redirectUrl = "/modulo/" + moduloId;
                    }
                    return ResponseEntity.status(HttpStatus.FOUND)
                            .header("Location", redirectUrl)
                            .build();
                }
            } catch (Exception e) {
                logger.error("Error al obtener la siguiente actividad: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", "/modulos")
                        .build();
            }
            
        } catch (Exception e) {
            logger.error("Error inesperado al obtener la siguiente actividad: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "application/json")
                    .body("{\"success\":false,\"message\":\"Error inesperado al obtener la siguiente actividad\"}");
        }
    }
    
    @PostMapping("/{id}/completar")
    public ResponseEntity<String> marcarComoCompletada(
            @PathVariable("id") String idStr,
            @RequestParam(name = "isCorrect", defaultValue = "false") boolean isCorrect,
            @RequestHeader(value = "X-CSRF-TOKEN", required = false) String csrfToken) {
        
        logger.info("=== SOLICITUD PARA MARCAR ACTIVIDAD COMO COMPLETADA ===");
        logger.info("ID de actividad recibido como string: {}", idStr);
        
        if (idStr == null || idStr.trim().isEmpty() || "undefined".equalsIgnoreCase(idStr)) {
            logger.error("ID de actividad no proporcionado o inválido: {}", idStr);
            return ResponseEntity.badRequest()
                    .body("{\"success\":false,\"message\":\"ID de actividad no proporcionado o inválido\"}");
        }
        
        Long id;
        try {
            id = Long.parseLong(idStr);
            logger.info("ID de actividad convertido a Long: {}", id);
        } catch (NumberFormatException e) {
            logger.error("Error al convertir el ID de actividad a número: {}", idStr);
            return ResponseEntity.badRequest()
                    .body("{\"success\":false,\"message\":\"ID de actividad no válido\"}");
        }
        
        logger.info("ID de actividad a marcar como completada: {}", id);
        
        try {
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            logger.info("Usuario autenticado: {}", auth != null ? auth.getName() : "NO AUTENTICADO");
            logger.info("CSRF Token recibido: {}", csrfToken != null ? "PRESENTE" : "AUSENTE");
            
            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
                logger.error("Usuario no autenticado o token inválido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"success\":false,\"message\":\"No autenticado o sesión expirada\"}");
            }
            
            logger.info("Iniciando marcado de actividad como completada...");
            boolean exito = activityService.marcarComoCompletada(id, isCorrect);
            
            if (exito) {
                logger.info("Actividad {} marcada como completada exitosamente", id);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"success\":true}");
            } else {
                logger.error("No se pudo marcar la actividad {} como completada", id);
                return ResponseEntity.badRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"success\":false,\"message\":\"No se pudo marcar la actividad como completada. Verifica los logs del servidor para más detalles.\"}");
            }
        } catch (Exception e) {
            logger.error("Error inesperado al marcar la actividad como completada", e);
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(String.format("{\"success\":false,\"message\":\"Error interno del servidor: %s\"}", 
                            e.getMessage() != null ? e.getMessage() : "Error desconocido"));
        }
    }
}
