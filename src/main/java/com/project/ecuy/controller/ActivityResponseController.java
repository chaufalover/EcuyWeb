package com.project.ecuy.controller;

import com.project.ecuy.entities.Activity;
import com.project.ecuy.entities.User;
import com.project.ecuy.entities.UserActivityProgress;
import com.project.ecuy.services.ActivityService;
import com.project.ecuy.services.UserActivityProgressService;
import com.project.ecuy.services.UserProgressService;
import com.project.ecuy.services.UserService;
import com.project.ecuy.entities.UserProgress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/activity-response")
public class ActivityResponseController {

    private final UserProgressService userProgressService;
    private final UserActivityProgressService userActivityProgressService;
    private final ActivityService activityService;
    private final UserService userService;

    @Autowired
    public ActivityResponseController(UserProgressService userProgressService,
                                    UserActivityProgressService userActivityProgressService,
                                    ActivityService activityService,
                                    UserService userService) {
        this.userProgressService = userProgressService;
        this.userActivityProgressService = userActivityProgressService;
        this.activityService = activityService;
        this.userService = userService;
    }

    /**
     * 
     * @param activityId 
     * @param isCorrect 
     * @return 
     */
    @PostMapping("/{activityId}/submit")
    public ResponseEntity<?> submitActivityResponse(
            @PathVariable Long activityId,
            @RequestParam(required = false, defaultValue = "false") boolean isCorrect) {
        
        try {
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = Optional.ofNullable(userService.buscarUsuario(username))
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            
            Activity activity = activityService.getById(activityId);
            if (activity == null) {
                return ResponseEntity.badRequest().body("{\"success\":false,\"message\":\"Actividad no encontrada\"}");
            }
            
            Long moduleId = activity.getModulo().getId();
            
            if (!userProgressService.canAccessModule(user.getId(), moduleId)) {
                return ResponseEntity.badRequest()
                        .body("{\"success\":false,\"message\":\"Debes completar el módulo anterior para acceder a este contenido\"}");
            }
            
            
            UserActivityProgress activityProgress = userActivityProgressService.findOrCreate(user.getId(), activityId);
            
            
            if (activityProgress.getCompletedAt() != null) {
                
                if (activityProgress.getPuntosObtenidos() == 0 && isCorrect) {
                    activityProgress.setPuntosObtenidos(5);
                    activityProgress = userActivityProgressService.save(activityProgress);
                    
                    
                    UserProgress updatedProgress = userProgressService.updateScore(user.getId(), moduleId, 5);
                    boolean moduloCompletado = updatedProgress.isCompletado();
                    int puntuacionActual = updatedProgress.getPuntuacion();
                    
                    return ResponseEntity.ok()
                            .body(String.format(
                                "{\"success\":true,\"message\":\"¡Respuesta correcta! Has ganado 5 puntos.\"," +
                                "\"puntosObtenidos\":5,\"puntuacionTotal\":%d,\"moduloCompletado\":%b,\"puntajeActualizado\":true}",
                                puntuacionActual, moduloCompletado));
                }
                
                
                return ResponseEntity.ok()
                        .body("{\"success\":true,\"message\":\"Actividad ya completada anteriormente\",\"alreadyCompleted\":true}");
            }
            
            if (isCorrect) {
                
                int puntosPorActividad = 5;
                
                activityProgress.setPuntosObtenidos(puntosPorActividad);
                activityProgress.setCompletedAt(java.time.LocalDateTime.now());
                userActivityProgressService.save(activityProgress);
                
                
                UserProgress updatedProgress = userProgressService.updateScore(user.getId(), moduleId, puntosPorActividad);
                
                
                boolean moduloCompletado = updatedProgress.isCompletado();
                int puntuacionActual = updatedProgress.getPuntuacion();
                
                return ResponseEntity.ok()
                        .body(String.format(
                            "{\"success\":true,\"message\":\"¡Respuesta correcta! Has ganado %d puntos.\"," +
                            "\"puntosObtenidos\":%d,\"puntuacionTotal\":%d,\"moduloCompletado\":%b}",
                            puntosPorActividad, puntosPorActividad, puntuacionActual, moduloCompletado));
            } else {
                
                activityProgress.setPuntosObtenidos(0);
                activityProgress.setCompletedAt(java.time.LocalDateTime.now());
                userActivityProgressService.save(activityProgress);
                
                
                UserProgress moduloProgress = userProgressService.findByUserAndModule(user.getId(), moduleId);
                
                return ResponseEntity.ok()
                        .body(String.format(
                            "{\"success\":false,\"message\":\"Respuesta incorrecta. Inténtalo de nuevo.\"," +
                            "\"puntosObtenidos\":0,\"puntuacionTotal\":%d,\"moduloCompletado\":%b}",
                            moduloProgress.getPuntuacion(), moduloProgress.isCompletado()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(String.format("{\"success\":false,\"message\":\"Error al procesar la respuesta: %s\"}", 
                            e.getMessage() != null ? e.getMessage() : "Error desconocido"));
        }
    }
    
    @GetMapping("/module/{moduleId}/progress")
    public ResponseEntity<?> getModuleProgress(@PathVariable Long moduleId) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = Optional.ofNullable(userService.buscarUsuario(username))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                
        
        var progress = userProgressService.findByUserAndModule(user.getId(), moduleId);
        
        return ResponseEntity.ok(progress);
    }
}
