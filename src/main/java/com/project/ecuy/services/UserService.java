package com.project.ecuy.services;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.ecuy.dto.RegisterRequest;
import com.project.ecuy.entities.User;
import com.project.ecuy.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;

    private final PasswordEncoder passwordEncoder;

    public void registrar(RegisterRequest req) {

        if (repository.findByUsuario(req.getUsuario()).isPresent()) {
            throw new RuntimeException("El nombre de usuario ya está en uso.");
        }

        if (repository.findByCorreo(req.getCorreo()).isPresent()) {
            throw new RuntimeException("El correo electrónico ya está registrado.");
        }

        String passwordHash = passwordEncoder.encode(req.getPassword());

        User usuario = new User(req.getNombre(), req.getApellido(), req.getCorreo(), passwordHash, "User", req.getUsuario());

        repository.save(usuario);
    }



    public void restablecerPasswordConToken(String token, String nuevaPassword) {
        User user = repository.findByResetToken(token)
            .filter(u -> u.getTokenExpiration().isAfter(LocalDateTime.now()))
            .orElseThrow(() -> new RuntimeException("Token inválido o expirado"));

        user.setPassword(passwordEncoder.encode(nuevaPassword));
        user.setResetToken(null);
        user.setTokenExpiration(null);
        repository.save(user);
    }
}
