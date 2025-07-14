package com.project.ecuy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.project.ecuy.entities.ModuleSurvey;
import com.project.ecuy.entities.User;
import com.project.ecuy.entities.Module;

@Repository
public interface ModuleSurveyRepository extends JpaRepository<ModuleSurvey, Long> {
    List<ModuleSurvey> findByModulo(Module modulo);

    List<ModuleSurvey> findByUsuario(User user);

    boolean existsByUsuarioAndModulo(User usuario, Module modulo);

    // Consulta para dashboard: cantidad por calificación en un módulo específico
    @Query("SELECT s.calificacion, COUNT(s) FROM ModuleSurvey s WHERE s.modulo.id = :moduleId GROUP BY s.calificacion")
    List<Object[]> countByCalificacionGrouped(Long moduleId);

    List<ModuleSurvey> findAllByOrderByFechaCreacionDesc();
}
