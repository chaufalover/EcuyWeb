package com.project.ecuy.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.ecuy.dto.RegisterRequest;
import com.project.ecuy.entities.User;
import com.project.ecuy.repository.UserRepository;
import com.project.ecuy.util.RolEnum;

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

        User usuario = new User(req.getNombre(), req.getApellido(), req.getCorreo(), passwordHash,
                RolEnum.USER.toString(), req.getUsuario());
        usuario.setNombre(capitalizar(usuario.getNombre()));
        usuario.setApellido(capitalizar(usuario.getApellido()));

        repository.save(usuario);
    }

    public void restablecerPasswordConToken(String token, String nuevaPassword) {
        User user = repository.findByResetToken(token).filter(u -> u.getTokenExpiration().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new RuntimeException("Token inválido o expirado"));

        user.setPassword(passwordEncoder.encode(nuevaPassword));
        user.setResetToken(null);
        user.setTokenExpiration(null);
        repository.save(user);
    }

    public List<User> selectAll() {
        return repository.findAll();
    }

    public User selectOne(Integer id) {
        return repository.findById(id).orElse(new User());
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }

    public User buscarUsuario(String username) {
        return repository.findByUsuario(username).orElseThrow();
    }

    public String capitalizar(String texto) {
        if (texto == null || texto.isEmpty())
            return texto;
        return texto.substring(0, 1).toUpperCase() + texto.substring(1).toLowerCase();
    }

    public void guardarUsuario(User usuario) {
        repository.save(usuario);
    }

    public int totalUsuarios() {
        return repository.contador();
    }

     public List<User> buscarUsuarios(String texto) {
        return repository.buscarUsuarios(texto);
    }

    public void cambiarNombreUsuario(String actualUsername, String nuevoUsername) {
        User usuario = repository.buscarUsuario(actualUsername);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }
        if (repository.existsByUsuario(nuevoUsername)) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso.");
        }
        usuario.setUsuario(nuevoUsername);
        repository.save(usuario);
    }

    public void cambiarCorreo(String actualUsername, String nuevoCorreo) {
        User usuario = repository.buscarUsuario(actualUsername);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }
        if (repository.existsByCorreo(nuevoCorreo)) {
            throw new IllegalArgumentException("El correo ya está en uso.");
        }
        usuario.setCorreo(nuevoCorreo);
        repository.save(usuario);
    }

}
