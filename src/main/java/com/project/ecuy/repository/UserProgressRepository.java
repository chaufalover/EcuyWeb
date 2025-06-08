package com.project.ecuy.repository;

import com.project.ecuy.entities.User;
import com.project.ecuy.entities.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    Optional<UserProgress> findByUsuarioIdAndModuloId(Integer usuarioId, Long moduloId);
    
    List<UserProgress> findByUsuarioIdOrderByFechaUltimoIntentoDesc(Integer usuarioId);
    
    @Query("SELECT up FROM UserProgress up WHERE up.usuario = :user AND up.modulo.activo = true")
    List<UserProgress> findActiveModulesProgressByUser(@Param("user") User user);
    
    @Query("SELECT SUM(up.puntuacion) FROM UserProgress up WHERE up.usuario = :user")
    Integer getTotalScoreByUser(@Param("user") User user);
}
