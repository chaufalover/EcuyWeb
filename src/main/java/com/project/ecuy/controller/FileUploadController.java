package com.project.ecuy.controller;

import com.project.ecuy.entities.User;
import com.project.ecuy.services.UserService;
import com.project.ecuy.services.ImageModerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FileUploadController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private ImageModerationService imageModerationService;

    @PostMapping("/upload-profile-image")
    public ResponseEntity<Object> uploadProfileImage(@RequestParam("image") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Por favor selecciona una imagen"
                ));
            }


            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Solo se permiten archivos de imagen"
                ));
            }

            
            Map<String, Object> moderationResult = imageModerationService.checkImage(file);
            if (!(boolean) moderationResult.getOrDefault("success", false)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", moderationResult.getOrDefault("message", "Error al verificar la imagen")
                ));
            }
            
            if (!(boolean) moderationResult.getOrDefault("isAppropriate", false)) {
                String rejectionReason = (String) moderationResult.getOrDefault("reasons", 
                    "La imagen contiene contenido inapropiado según nuestras políticas.");
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", rejectionReason
                ));
            }

            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userService.buscarUsuario(username);

            if (user == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "Usuario no encontrado"
                ));
            }

            
            user.setFotoPerfil(file.getBytes());
            user.setTipoImagen(contentType);
            userService.guardarUsuario(user);

            
            return ResponseEntity.ok().body(Map.of(
                "success", true,
                "message", "Imagen de perfil actualizada exitosamente"
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Error al procesar la imagen: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/profile-image")
    public ResponseEntity<byte[]> getProfileImage() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                return getDefaultImage();
            }
            
            String username = auth.getName();
            User user = userService.buscarUsuario(username);

            if (user == null || user.getFotoPerfil() == null || user.getFotoPerfil().length == 0) {
                return getDefaultImage();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(user.getTipoImagen()))
                    .cacheControl(CacheControl.noCache())
                    .body(user.getFotoPerfil());
        } catch (Exception e) {
            return getDefaultImage();
        }
    }
    
    private ResponseEntity<byte[]> getDefaultImage() {
        try {
            Resource resource = new ClassPathResource("static/img/perfil.PNG");
            byte[] defaultImage = StreamUtils.copyToByteArray(resource.getInputStream());
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .cacheControl(CacheControl.noCache())
                    .body(defaultImage);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
