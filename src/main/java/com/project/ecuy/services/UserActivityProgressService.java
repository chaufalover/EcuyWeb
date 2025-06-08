package com.project.ecuy.services;

import com.project.ecuy.entities.Activity;
import com.project.ecuy.entities.User;
import com.project.ecuy.entities.UserActivityProgress;
import com.project.ecuy.exception.ResourceNotFoundException;
import com.project.ecuy.repository.ActivityRepository;
import com.project.ecuy.repository.UserActivityProgressRepository;
import com.project.ecuy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserActivityProgressService {

    private final UserActivityProgressRepository repository;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;

    @Autowired
    public UserActivityProgressService(UserActivityProgressRepository repository,
                                     UserRepository userRepository,
                                     ActivityRepository activityRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
    }

    @Transactional
    public UserActivityProgress findOrCreate(Integer userId, Long activityId) {
        return repository.findByUserIdAndActivityId(userId, activityId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                    Activity activity = activityRepository.findById(activityId)
                            .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + activityId));
                    
                    UserActivityProgress progress = new UserActivityProgress();
                    progress.setUser(user);
                    progress.setActivity(activity);
                    progress.setStartedAt(LocalDateTime.now());
                    progress.setPuntosObtenidos(0);
                    progress.setPuntosTotales(5); // 5 puntos por actividad
                    
                    return repository.save(progress);
                });
    }
    
    @Transactional
    public UserActivityProgress save(UserActivityProgress progress) {
        return repository.save(progress);
    }
    
    public Optional<UserActivityProgress> findByUserAndActivity(Integer userId, Long activityId) {
        return repository.findByUserIdAndActivityId(userId, activityId);
    }
}
