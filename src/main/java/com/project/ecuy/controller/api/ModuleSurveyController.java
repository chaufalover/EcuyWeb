package com.project.ecuy.controller.api;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.ecuy.entities.ModuleSurvey;
import com.project.ecuy.auth.CustomUserDetail;
import com.project.ecuy.entities.Module;
import com.project.ecuy.entities.User;
import com.project.ecuy.repository.ModuleRepository;
import com.project.ecuy.repository.UserRepository;
import com.project.ecuy.services.ModuleService;
import com.project.ecuy.services.ModuleSurveyService;
import com.project.ecuy.services.UserService;
import com.project.ecuy.util.RatingEnum;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RestController
@RequestMapping("/api/encuestas")
public class ModuleSurveyController {

    @Autowired
    private ModuleSurveyService surveyService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @PostMapping
    public ResponseEntity<String> guardarEncuesta(@RequestBody EncuestaRequest dto, Principal principal) {
        try {
            // Obtener correo del usuario autenticado
            CustomUserDetail userDetails = (CustomUserDetail) ((Authentication) principal).getPrincipal();
            String correoUsuario = userDetails.getUsername();

            System.out.println("Correo extraído del usuario autenticado: " + correoUsuario);

            // Buscar usuario por correo
            User user = userRepository.findByUsuario(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Buscar módulo
            Module modulo = moduleRepository.findById(dto.getModuloId())
                    .orElseThrow(() -> new RuntimeException("Módulo no encontrado"));

            // Crear y guardar la encuesta
            ModuleSurvey encuesta = ModuleSurvey.builder().usuario(user).modulo(modulo)
                    .calificacion(dto.getCalificacion()).comentario(dto.getComentario()).build();

            surveyService.guardarEncuesta(encuesta);
            return ResponseEntity.ok("Encuesta guardada exitosamente");

        } catch (Exception e) {
            e.printStackTrace(); // Para ver traza completa
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar encuesta: " + e.getMessage());
        }
    }

    @GetMapping("/verificar")
    public ResponseEntity<Boolean> yaRespondioEncuesta(@RequestParam Long moduloId, Principal principal) {
        User user = userRepository.findByUsuario(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Module modulo = moduleRepository.findById(moduloId)
                .orElseThrow(() -> new RuntimeException("Módulo no encontrado"));

        boolean yaRespondio = surveyService.encuestaYaRegistrada(user, modulo);
        return ResponseEntity.ok(yaRespondio);
    }

    @Getter
    @Setter
    public static class EncuestaRequest {
        private Long moduloId;
        private RatingEnum calificacion; // Asegúrate de enviar "BUENO", "REGULAR", etc.
        private String comentario;
    }
}
