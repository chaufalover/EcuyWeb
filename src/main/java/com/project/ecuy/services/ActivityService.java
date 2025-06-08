package com.project.ecuy.services;

import com.project.ecuy.entities.Activity;
import com.project.ecuy.entities.Module;
import com.project.ecuy.entities.User;
import com.project.ecuy.entities.UserActivityProgress;
import com.project.ecuy.entities.VideoWord;
import com.project.ecuy.exception.ResourceNotFoundException;
import com.project.ecuy.repository.ActivityRepository;
import com.project.ecuy.repository.ModuleRepository;
import com.project.ecuy.repository.UserActivityProgressRepository;
import com.project.ecuy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ActivityService extends BaseService<Activity, Long, ActivityRepository> {

    private static final Logger logger = LoggerFactory.getLogger(ActivityService.class);
    private final ModuleRepository moduleRepository;
    private final UserActivityProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final UserProgressService userProgressService;

    @Autowired
    public ActivityService(ActivityRepository repository, 
                         ModuleRepository moduleRepository,
                         UserActivityProgressRepository progressRepository,
                         UserRepository userRepository,
                         UserProgressService userProgressService) {
        super(repository);
        this.moduleRepository = moduleRepository;
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.userProgressService = userProgressService;
    }

    public List<Activity> findByModuleId(Long moduleId) {
        return repository.findByModuloIdOrderByOrdenAsc(moduleId);
    }

    /**
     * 
     * @param currentActivityId 
     * @return 
     */
    public List<Activity> obtenerSiguientesActividades(Long currentActivityId) {
        return repository.findSiguientesActividades(currentActivityId);
    }

    public Activity createActivityForModule(Activity activity, Long moduleId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id " + moduleId));
        activity.setModulo(module);
        return repository.save(activity);
    }

    @Transactional
    public boolean marcarComoCompletada(Long actividadId, boolean isCorrect) {
        logger.info("=== INICIO marcarComoCompletada para actividadId: {} ===", actividadId);
        
        try {
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            logger.info("Contexto de seguridad obtenido. Autenticado: {}", 
                (authentication != null && authentication.isAuthenticated()));
                
            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
                logger.error("Usuario no autenticado o token inválido");
                return false;
            }
            
            String username = authentication.getName();
            logger.info("Buscando usuario: {}", username);
            
            
            Optional<User> userOpt = userRepository.findByUsuario(username);
            if (userOpt.isEmpty()) {
                logger.error("Usuario no encontrado en la base de datos: {}", username);
                return false;
            }
            
            User user = userOpt.get();
            logger.info("Usuario encontrado - ID: {}, Nombre: {}", user.getId(), user.getUsuario());
                
            
            logger.info("Buscando actividad con ID: {}", actividadId);
            Optional<Activity> actividadOpt = repository.findById(actividadId);
            if (actividadOpt.isEmpty()) {
                logger.error("Actividad no encontrada con ID: {}", actividadId);
                return false;
            }
            
            Activity actividad = actividadOpt.get();
            logger.info("Actividad encontrada - ID: {}, Título: {}, Tipo: {}", 
                actividad.getId(), actividad.getTitulo(), actividad.getTipoActividad());
            
            
            logger.info("Buscando progreso existente para usuario {} y actividad {}", user.getId(), actividadId);
            Optional<UserActivityProgress> progresoExistente = progressRepository.findByUserAndActivity(user, actividad);
            
            
            boolean otorgaPuntos = false;
            int puntos = 0;
            
            if (actividad.getTipoActividad() != null) {
                String tipoActividad = actividad.getTipoActividad().toUpperCase();
                
                if (tipoActividad.equals("VIDEO") || 
                    tipoActividad.equals("ROMPECABEZAS") || 
                    tipoActividad.equals("PUZZLE") || 
                    tipoActividad.equals("QUIZ") || 
                    tipoActividad.equals("CUESTIONARIO") || 
                    tipoActividad.equals("RELACIONAR") || 
                    tipoActividad.equals("EMPAREJAR") ||
                    tipoActividad.equals("MATCHING")) {
                    
                    otorgaPuntos = isCorrect; 
                    puntos = isCorrect ? 5 : 0; 
                    
                    logger.info("La actividad de tipo '{}' - Respuesta {}. Puntos a otorgar: {}", 
                        tipoActividad, isCorrect ? "correcta" : "incorrecta", puntos);
                } else {
                    logger.info("La actividad de tipo '{}' no otorga puntos", tipoActividad);
                }
            } else {
                logger.warn("Tipo de actividad no especificado, no se otorgarán puntos");
            }
                
            if (progresoExistente.isPresent()) {
                
                UserActivityProgress progreso = progresoExistente.get();
                logger.info("Progreso existente encontrado - ID: {}", progreso.getId());
                
                if (progreso.getCompletedAt() == null) {
                    progreso.setCompletedAt(LocalDateTime.now());
                    
                    
                    if (otorgaPuntos) {
                        progreso.setPuntosObtenidos(puntos);
                        progreso.setPuntosTotales(puntos);
                    }
                    
                    logger.info("Actualizando progreso existente");
                    progressRepository.save(progreso);
                    logger.info("Actividad marcada como completada exitosamente (actualización)");
                    
                    
                    if (otorgaPuntos && actividad.getModulo() != null) {
                        try {
                            userProgressService.updateScore(user.getId(), actividad.getModulo().getId(), puntos);
                            logger.info("Puntos actualizados para el módulo {} - Usuario: {}, Puntos: {}", 
                                actividad.getModulo().getId(), user.getId(), puntos);
                        } catch (Exception e) {
                            logger.error("Error al actualizar el progreso del módulo: {}", e.getMessage(), e);
                        }
                    }
                    
                    return true;
                } else {
                    logger.info("La actividad ya estaba marcada como completada anteriormente");
                    return true; 
                }
            } else {
                
                logger.info("Creando nuevo progreso");
                UserActivityProgress nuevoProgreso = new UserActivityProgress();
                nuevoProgreso.setUser(user);
                nuevoProgreso.setActivity(actividad);
                nuevoProgreso.setStartedAt(LocalDateTime.now());
                nuevoProgreso.setCompletedAt(LocalDateTime.now());
                
                
                if (otorgaPuntos) {
                    nuevoProgreso.setPuntosObtenidos(puntos);
                    nuevoProgreso.setPuntosTotales(puntos);
                }
                
                try {
                    UserActivityProgress progresoGuardado = progressRepository.save(nuevoProgreso);
                    logger.info("Nuevo progreso creado exitosamente - ID: {}", progresoGuardado.getId());
                    
                    
                    if (otorgaPuntos && actividad.getModulo() != null) {
                        try {
                            userProgressService.updateScore(user.getId(), actividad.getModulo().getId(), puntos);
                            logger.info("Puntos actualizados para el módulo {} - Usuario: {}, Puntos: {}", 
                                actividad.getModulo().getId(), user.getId(), puntos);
                        } catch (Exception e) {
                            logger.error("Error al actualizar el progreso del módulo: {}", e.getMessage(), e);
                        }
                    }
                    
                    return true;
                } catch (Exception e) {
                    logger.error("Error al guardar el progreso: {}", e.getMessage(), e);
                    return false;
                }
            }
        } catch (Exception e) {
            logger.error("Error inesperado al marcar la actividad como completada: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 
     * @param id 
     * @return 
     * @throws ResourceNotFoundException 
     */
    @Transactional(readOnly = true)
    public Activity getActivityWithContents(Long id) {
        return repository.findByIdWithContents(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + id));
    }

    public Activity updateActivity(Long id, Activity activityDetails) {
        Activity activity = getById(id);
        
        activity.setTitulo(activityDetails.getTitulo());
        activity.setDescripcion(activityDetails.getDescripcion());
        activity.setTipoActividad(activityDetails.getTipoActividad());
        activity.setOrden(activityDetails.getOrden());
        activity.setPuntos(activityDetails.getPuntos());
        activity.setActivo(activityDetails.isActivo());
        
        return repository.save(activity);
    }

    public Activity getActivityWithQuizOptions(Long id) {
        return repository.findByIdWithQuizOptions(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + id));
    }

    public Activity getActivityWithMatchingPairs(Long id) {
        return repository.findByIdWithMatchingPairs(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + id));
    }
    
    public Activity getActivityWithVideoWords(Long id) {
        return repository.findByIdWithVideoWords(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + id));
    }
    
    public Activity getActivityWithOrderedVideoWords(Long id) {
        return repository.findByIdWithOrderedVideoWords(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + id));
    }
    
    @Transactional
    public void addVideoWordsToActivity(Long activityId, List<VideoWord> words) {
        Activity activity = getById(activityId);
        for (VideoWord word : words) {
            word.setActividad(activity);
        }
        activity.getPalabrasVideo().addAll(words);
        repository.save(activity);
    }
    
    /**
     * 
     * @param activityId 
     * @param imageUrl 
     * @return 
     */
    @Transactional
    public Activity updatePuzzleConfig(Long activityId, String imageUrl) {
        Activity activity = getById(activityId);
        activity.setUrlImagenRompecabezas(imageUrl);
        return repository.save(activity);
    }
    
    /**
     * 
     * @param activityId 
     * @return 
     */
    @Transactional(readOnly = true)
    public Activity getPuzzleConfig(Long activityId) {
        return repository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + activityId));
    }
}
