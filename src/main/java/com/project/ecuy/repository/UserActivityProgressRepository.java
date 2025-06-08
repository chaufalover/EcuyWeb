package com.project.ecuy.repository;

import com.project.ecuy.entities.Activity;
import com.project.ecuy.entities.User;
import com.project.ecuy.entities.UserActivityProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserActivityProgressRepository extends JpaRepository<UserActivityProgress, Long> {
    Optional<UserActivityProgress> findByUserAndActivity(User user, Activity activity);
    
    @Query("SELECT uap FROM UserActivityProgress uap WHERE uap.user.id = :userId AND uap.activity.id = :activityId")
    Optional<UserActivityProgress> findByUserIdAndActivityId(@Param("userId") Integer userId, 
                                                           @Param("activityId") Long activityId);
    
    @Query("SELECT uap FROM UserActivityProgress uap WHERE uap.user.id = :userId AND uap.activity.modulo.id = :moduleId")
    List<UserActivityProgress> findByUserIdAndModuleId(@Param("userId") Integer userId, 
                                                      @Param("moduleId") Long moduleId);
    
    @Query("SELECT COALESCE(SUM(uap.puntosObtenidos), 0) FROM UserActivityProgress uap " +
          "WHERE uap.user.id = :userId AND uap.activity.modulo.id = :moduleId")
    Integer sumPuntosByUserIdAndModuleId(@Param("userId") Integer userId, 
                                        @Param("moduleId") Long moduleId);
}
