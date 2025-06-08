package com.project.ecuy.services;

import com.project.ecuy.entities.Module;
import com.project.ecuy.entities.User;
import com.project.ecuy.entities.UserProgress;
import com.project.ecuy.exception.ResourceNotFoundException;
import com.project.ecuy.repository.ModuleRepository;
import com.project.ecuy.repository.UserProgressRepository;
import com.project.ecuy.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserProgressService extends BaseService<UserProgress, Long, UserProgressRepository> {

    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;

    @Autowired
    public UserProgressService(UserProgressRepository repository, 
                             UserRepository userRepository,
                             ModuleRepository moduleRepository) {
        super(repository);
        this.userRepository = userRepository;
        this.moduleRepository = moduleRepository;
    }

    public List<UserProgress> findByUserId(Integer userId) {
        return repository.findByUsuarioIdOrderByFechaUltimoIntentoDesc(userId);
    }
    
    /**
     * 
     * 
     * @param userId ID del usuario
     * @param moduleId ID del módulo
     * @return El progreso del usuario o null si no existe
     */
    public UserProgress getUserProgressWithoutCreating(Integer userId, Long moduleId) {
        return repository.findByUsuarioIdAndModuloId(userId, moduleId).orElse(null);
    }

    public UserProgress findByUserAndModule(Integer userId, Long moduleId) {
        return repository.findByUsuarioIdAndModuloId(userId, moduleId)
                .orElseGet(() -> createNewUserProgress(userId, moduleId));
    }

    /**
     * 
     * @param userId 
     * @param moduleId 
     * @param puntosGanados 
     * @return 
     */
    @Transactional
    public UserProgress updateScore(Integer userId, Long moduleId, int puntosGanados) {
        
        if (puntosGanados < 0 || puntosGanados > 5) {
            throw new IllegalArgumentException("Los puntos deben estar entre 0 y 5 por actividad");
        }
        
        
        UserProgress progress = repository.findByUsuarioIdAndModuloId(userId, moduleId)
                .orElseGet(() -> createNewUserProgress(userId, moduleId));
        
        
        if (progress.getPuntuacion() >= 20) {
            return progress;
        }
        
        int nuevoPuntaje = Math.min(progress.getPuntuacion() + puntosGanados, 20);
        
        progress.setPuntuacion(nuevoPuntaje);
        progress.setCompletado(nuevoPuntaje >= 10); 
        progress.setFechaUltimoIntento(java.time.LocalDateTime.now());
        
        return repository.save(progress);
    }

    private UserProgress createNewUserProgress(Integer userId, Long moduleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + moduleId));
        
        UserProgress progress = new UserProgress();
        progress.setUsuario(user);
        progress.setModulo(module);
        progress.setPuntuacion(0);
        progress.setCompletado(false);
        
        return repository.save(progress);
    }

    
    public Integer getTotalScoreByUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Integer score = repository.getTotalScoreByUser(user);
        return score != null ? score : 0;
    }
    
    /**
     * 
     * @param userId 
     * @param moduleId 
     * @return 
     */
    public boolean canAccessModule(Integer userId, Long moduleId) {
        if (moduleId == 1) {
            return true; 
        }
        
        Long previousModuleId = moduleId - 1;
        UserProgress previousProgress = repository.findByUsuarioIdAndModuloId(userId, previousModuleId)
                .orElseGet(() -> createNewUserProgress(userId, previousModuleId));
        
        return previousProgress.isCompletado() && previousProgress.getPuntuacion() >= 10;
    }
}
