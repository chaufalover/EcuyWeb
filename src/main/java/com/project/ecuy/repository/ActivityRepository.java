package com.project.ecuy.repository;

import com.project.ecuy.entities.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByModuloId(Long moduloId);
    List<Activity> findByModuloIdOrderByOrdenAsc(Long moduloId);
    
    @Query("SELECT DISTINCT a FROM Activity a LEFT JOIN FETCH a.contenidos WHERE a.id = :id")
    Optional<Activity> findByIdWithContents(@Param("id") Long id);
    
    @Query("SELECT DISTINCT a FROM Activity a LEFT JOIN FETCH a.opcionesQuiz WHERE a.id = :id")
    Optional<Activity> findByIdWithQuizOptions(@Param("id") Long id);
    
    @Query("SELECT DISTINCT a FROM Activity a LEFT JOIN FETCH a.paresRelacionados WHERE a.id = :id")
    Optional<Activity> findByIdWithMatchingPairs(@Param("id") Long id);
    
    @Query("SELECT DISTINCT a FROM Activity a LEFT JOIN FETCH a.palabrasVideo WHERE a.id = :id")
    Optional<Activity> findByIdWithVideoWords(@Param("id") Long id);
    
    @Query("SELECT a FROM Activity a " +
           "WHERE a.modulo.id = (SELECT a2.modulo.id FROM Activity a2 WHERE a2.id = :currentActivityId) " +
           "AND a.orden > (SELECT a3.orden FROM Activity a3 WHERE a3.id = :currentActivityId) " +
           "ORDER BY a.orden ASC")
    List<Activity> findSiguientesActividades(@Param("currentActivityId") Long currentActivityId);
    
    @Query("SELECT DISTINCT a FROM Activity a LEFT JOIN FETCH a.palabrasVideo pv WHERE a.id = :id ORDER BY pv.ordenCorrecto")
    Optional<Activity> findByIdWithOrderedVideoWords(@Param("id") Long id);
    
    
    @Query("SELECT a FROM Activity a " +
           "WHERE a.modulo.id = (SELECT a2.modulo.id FROM Activity a2 WHERE a2.id = :currentActivityId) " +
           "AND a.orden < (SELECT a2.orden FROM Activity a2 WHERE a2.id = :currentActivityId) " +
           "AND a.activo = true " +
           "ORDER BY a.orden DESC")
    List<Activity> findPreviousActivities(@Param("currentActivityId") Long currentActivityId);
    
    @Query("SELECT a FROM Activity a " +
           "WHERE a.modulo.id = (SELECT a2.modulo.id FROM Activity a2 WHERE a2.id = :currentActivityId) " +
           "AND a.orden > (SELECT a2.orden FROM Activity a2 WHERE a2.id = :currentActivityId) " +
           "AND a.activo = true " +
           "ORDER BY a.orden ASC")
    List<Activity> findNextActivities(@Param("currentActivityId") Long currentActivityId);
}
