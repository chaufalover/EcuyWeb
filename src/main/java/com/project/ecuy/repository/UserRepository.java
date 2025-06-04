package com.project.ecuy.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.ecuy.entities.User;

public interface UserRepository extends JpaRepository<User, Integer> {
     Optional<User> findByUsuario(String usuario);
     Optional<User> findByCorreo(String correo);
}
