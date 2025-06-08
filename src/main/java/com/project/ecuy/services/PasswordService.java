package com.project.ecuy.services;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.project.ecuy.entities.User;
import com.project.ecuy.repository.UserRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordService {
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    public void enviarLinkRecuperacion(String correo) {
        User user = userRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Correo no registrado"));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setTokenExpiration(LocalDateTime.now().plusMinutes(30));
        userRepository.save(user);

        String link = "http://localhost:8080/reset-password?token=" + token;

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setTo(correo);
            helper.setSubject("Recuperación de contraseña - Ecuy");
            helper.setText(
                    "<p>Haz clic en el siguiente botón para restablecer tu contraseña:</p>" +
                            "<a href='" + link + "' " +
                            "style='display:inline-block;padding:10px 20px;background-color:#007bff;color:white;text-decoration:none;border-radius:5px;'>Restablecer contraseña</a>",
                    true);

            mailSender.send(mensaje);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar correo", e);
        }
    }
}
