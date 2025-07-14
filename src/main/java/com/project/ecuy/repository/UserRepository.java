package com.project.ecuy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.ecuy.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
     Optional<User> findByUsuario(String usuario);

     Optional<User> findByCorreo(String correo);

     Optional<User> findByResetToken(String token);

     @Query("SELECT u FROM User u WHERE u.usuario = :usuario")
     User buscarUsuario(@Param("usuario") String usuario);

     boolean existsByUsuario(String usuario);

     boolean existsByCorreo(String correo);

     @Query("select count(u) from User u")
     int contador();

      
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(u.apellido) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(u.correo) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(u.usuario) LIKE LOWER(CONCAT('%', :texto, '%'))")
    List<User> buscarUsuarios(@Param("texto") String texto);
}
