package com.project.ecuy.repository;

import com.project.ecuy.entities.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    List<Module> findAllByActivoTrueOrderByFechaCreacionAsc();
    
    @Query(value = "SELECT * FROM modulos WHERE activo = TRUE ORDER BY fecha_creacion ASC", nativeQuery = true)
    List<Module> findAllActiveModulesNative();
    
    boolean existsByNombre(String nombre);
    
    @Query("SELECT DISTINCT m FROM Module m LEFT JOIN FETCH m.actividades a WHERE m.id = :id AND m.activo = true AND (a IS NULL OR a.activo = true) ORDER BY COALESCE(a.orden, 0) ASC")
    Optional<Module> findByIdWithActivities(@Param("id") Long id);
}
