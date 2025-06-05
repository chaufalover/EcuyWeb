package com.project.ecuy.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.ecuy.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
     Optional<User> findByUsuario(String usuario);
     Optional<User> findByCorreo(String correo);
     Optional<User> findByResetToken(String token);
}
