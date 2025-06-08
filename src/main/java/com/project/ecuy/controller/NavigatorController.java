package com.project.ecuy.controller;

import com.project.ecuy.dto.ActivityDTO;
import com.project.ecuy.entities.Activity;
import com.project.ecuy.entities.Module;
import com.project.ecuy.entities.User;
import com.project.ecuy.repository.ActivityRepository;
import com.project.ecuy.repository.ModuleRepository;
import com.project.ecuy.repository.UserRepository;
import com.project.ecuy.services.ActivityService;
import com.project.ecuy.services.UserProgressService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping
public class NavigatorController {

    private static final Logger logger = LoggerFactory.getLogger(NavigatorController.class);
    private final ModuleRepository moduleRepository;
    private final ActivityRepository activityRepository;
    private final UserProgressService userProgressService;
    private final ActivityService activityService;
    private final UserRepository userRepository;

    @Autowired
    public NavigatorController(ModuleRepository moduleRepository, 
                            ActivityRepository activityRepository,
                            UserProgressService userProgressService,
                            ActivityService activityService,
                            UserRepository userRepository) {
        this.moduleRepository = moduleRepository;
        this.activityRepository = activityRepository;
        this.userProgressService = userProgressService;
        this.activityService = activityService;
        this.userRepository = userRepository;
    }

    @GetMapping("/modulos")
    public String modules(Model model) {
        logger.info("=== INICIO DE SOLICITUD /modulos ===");
        
        try {
            logger.info("Buscando módulos activos con JPA...");
            List<Module> modules = moduleRepository.findAllByActivoTrueOrderByFechaCreacionAsc();
            logger.info("Módulos encontrados con JPA: {}", modules.size());
            
            if (!modules.isEmpty()) {
                logger.info("=== Módulos JPA ===");
                modules.forEach(module -> 
                    logger.info("ID: {}, Nombre: {}, Activo: {}", 
                        module.getId(), module.getNombre(), module.isActivo())
                );
            } else {
                logger.warn("No se encontraron módulos, creando uno de prueba...");
                Module testModule = new Module();
                testModule.setId(1L);
                testModule.setNombre("Módulo de prueba");
                testModule.setActivo(true);
                testModule.setFechaCreacion(LocalDateTime.now());
                testModule.setFechaActualizacion(LocalDateTime.now());
                modules = List.of(testModule);
                logger.info("Módulo de prueba creado: {}", testModule);
                
                try {
                    moduleRepository.save(testModule);
                    logger.info("Módulo de prueba guardado en la base de datos");
                } catch (Exception e) {
                    logger.error("Error al guardar el módulo de prueba", e);
                }
            }
            
            logger.info("Buscando módulos con consulta nativa...");
            List<Module> nativeModules = moduleRepository.findAllActiveModulesNative();
            logger.info("Módulos encontrados con consulta nativa: {}", nativeModules.size());
            
            
            if (!nativeModules.isEmpty()) {
                logger.info("=== Módulos Nativa ===");
                nativeModules.forEach(module -> 
                    logger.info("ID: {}, Nombre: {}, Activo: {}", 
                        module.getId(), module.getNombre(), module.isActivo())
                );
            }
            
            if ((modules == null || modules.isEmpty()) && !nativeModules.isEmpty()) {
                logger.info("Usando módulos de la consulta nativa");
                modules = nativeModules;
            }
            
            if (modules == null) {
                modules = new ArrayList<>();
                logger.info("Lista de módulos inicializada como vacía");
            }
            
            if (!modules.isEmpty()) {
                for (Module module : modules) {
                    logger.info("Módulo - ID: {}, Nombre: {}, Activo: {}", 
                        module.getId(), module.getNombre(), module.isActivo());
                }
            } else {
                logger.warn("No se encontraron módulos activos en ninguna de las consultas");
            }
            
            List<Map<String, Object>> modulesData = new ArrayList<>();
            for (Module module : modules) {
                Map<String, Object> moduleData = new HashMap<>();
                moduleData.put("id", module.getId());
                moduleData.put("nombre", module.getNombre());
                moduleData.put("activo", module.isActivo());
                modulesData.add(moduleData);
            }
            
            String modulesJson = "[]";
            
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                modulesJson = objectMapper.writeValueAsString(modulesData);
                logger.debug("JSON de módulos generado correctamente");
                logger.info("JSON generado: {}", modulesJson);
            } catch (Exception e) {
                logger.error("Error al convertir módulos a JSON", e);
                modulesJson = "[]";
            }
            
            model.addAttribute("modules", modules);
            model.addAttribute("modulesJson", modulesJson);
            
            model.addAttribute("modulesJsonString", modulesJson);
            
            logger.info("Atributos del modelo:");
            model.asMap().forEach((key, value) -> 
                logger.info("  {}: {}", key, value != null ? value.toString() : "null")
            );
            
        } catch (Exception e) {
            logger.error("Error en el controlador /modulos", e);
            model.addAttribute("modulesJson", "[]");
        }
        
        return "modules";
    }

    @GetMapping("/api/modulos/{moduleId}/can-access")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> canAccessModule(@PathVariable("moduleId") Long moduleId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepository.findByUsuario(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
            
            boolean canAccess = userProgressService.canAccessModule(user.getId(), moduleId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("canAccess", canAccess);
            response.put("message", canAccess ? 
                "Puedes acceder a este módulo." : 
                "Debes completar el módulo anterior con al menos 10 puntos para desbloquear este módulo.");
                
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al verificar acceso al módulo: " + moduleId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al verificar el acceso al módulo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/modulo/{id}")
    public String chapter(@PathVariable("id") Long id, Model model) {
        try {
            logger.info("=== INICIO: Cargando módulo con ID: {} ===", id);
            
            
            logger.info("Verificando existencia del módulo con ID: {}", id);
            boolean moduleExists = moduleRepository.existsById(id);
            logger.info("Módulo con ID {} existe: {}", id, moduleExists);
            
            if (!moduleExists) {
                logger.error("ERROR: No se encontró el módulo con ID: {}", id);
                return "redirect:/modulos";
            }
            
            
            logger.info("Buscando módulo en la base de datos con actividades...");
            Optional<Module> moduloOpt = moduleRepository.findByIdWithActivities(id);
            
            if (moduloOpt.isEmpty()) {
                logger.error("ERROR: No se pudo cargar el módulo con ID: {}", id);
                return "redirect:/modulos";
            }
            
            Module modulo = moduloOpt.get();
            logger.info("Módulo encontrado: {}", modulo.getNombre());
            logger.info("ID del módulo: {}", modulo.getId());
            logger.info("Activo: {}", modulo.isActivo());
            logger.info("Fecha de creación: {}", modulo.getFechaCreacion());
            logger.info("Descripción: {}", modulo.getDescripcion());
            
            if (!modulo.isActivo()) {
                logger.warn("El módulo con ID {} está inactivo", id);
                model.addAttribute("error", "El módulo solicitado no está disponible");
                return "redirect:/modulos?error=module_inactive";
            }
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
                try {
                    UserDetails userDetails = (UserDetails) auth.getPrincipal();
                    User user = userRepository.findByUsuario(userDetails.getUsername())
                        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + userDetails.getUsername()));
                    
                    if (id > 1 && !userProgressService.canAccessModule(user.getId(), id)) {
                        logger.warn("El usuario {} no tiene acceso al módulo {}", user.getUsuario(), id);
                        model.addAttribute("error", "No tienes acceso a este módulo. Debes completar el módulo anterior con al menos 10 puntos.");
                        model.addAttribute("showAccessDeniedModal", true);
                    }
                    
                    
                    var progress = userProgressService.getUserProgressWithoutCreating(user.getId(), id);
                    int puntuacion = 0;
                    boolean moduloCompletado = false;
                    
                    if (progress != null) {
                        puntuacion = progress.getPuntuacion();
                        moduloCompletado = progress.isCompletado();
                    }
                    
                    int porcentajeProgreso = (int) Math.round((puntuacion / 20.0) * 100);
                    
                    model.addAttribute("puntuacion", puntuacion);
                    model.addAttribute("porcentajeProgreso", porcentajeProgreso);
                    model.addAttribute("moduloCompletado", moduloCompletado);
                    
                    logger.info("Progreso del usuario - Puntuación: {}, Porcentaje: {}%", 
                        puntuacion, porcentajeProgreso);
                } catch (Exception e) {
                    logger.error("Error al verificar acceso al módulo: {}", e.getMessage());
                    
                    model.addAttribute("puntuacion", 0);
                    model.addAttribute("porcentajeProgreso", 0);
                    model.addAttribute("moduloCompletado", false);
                }
            } else {
                
                model.addAttribute("puntuacion", 0);
                model.addAttribute("porcentajeProgreso", 0);
                model.addAttribute("moduloCompletado", false);
            }
            
            List<Activity> actividades = modulo.getActividades() != null ? 
                new ArrayList<>(modulo.getActividades()) : 
                Collections.emptyList();
                
            logger.info("Número de actividades encontradas: {}", actividades.size());
            
            for (Activity actividad : actividades) {
                logger.info("Actividad cargada - ID: {}, Título: {}, Tipo: {}, Orden: {}, Activa: {}", 
                    actividad.getId(), 
                    actividad.getTitulo() != null ? actividad.getTitulo() : "Sin título", 
                    actividad.getTipoActividad() != null ? actividad.getTipoActividad() : "Sin tipo",
                    actividad.getOrden(),
                    actividad.isActivo());
            }
            
            model.addAttribute("modulo", modulo);
            model.addAttribute("actividades", actividades);
            model.addAttribute("sinActividades", actividades.isEmpty());
            
            if (actividades.isEmpty()) {
                logger.warn("El módulo {} no tiene actividades activas", modulo.getNombre());
            }
            
            String tituloPagina = modulo.getNombre() + " - Ecuy";
            model.addAttribute("tituloPagina", tituloPagina);
            
            
            logger.info("=== RESUMEN DEL MODELO ===");
            logger.info("Título de la página: {}", tituloPagina);
            model.asMap().forEach((key, value) -> {
                logger.info("Atributo: {} = {}", key, value);
            });
            
            logger.info("=== FIN: Módulo cargado exitosamente ===");
            return "chapters-simple";
            
        } catch (Exception e) {
            logger.error("Error al cargar el módulo con ID: " + id, e);
            
            model.addAttribute("error", "Ocurrió un error al cargar el módulo");
            return "redirect:/modulos?error=load_error";
        }
    }

    @GetMapping("/actividad/{id}/siguiente")
    public String obtenerSiguienteActividad(
            @PathVariable("id") String idStr,
            @RequestParam(name = "moduloId", required = false) Long moduloId) {
        
        logger.info("=== OBTENIENDO SIGUIENTE ACTIVIDAD ===");
        logger.info("ID de actividad actual: {}", idStr);
        logger.info("ID de módulo: {}", moduloId);
        
        
        if (idStr == null || idStr.trim().isEmpty() || "undefined".equalsIgnoreCase(idStr)) {
            logger.error("ID de actividad no proporcionado o inválido: {}", idStr);
            return "redirect:/modulos";
        }
        
        
        Long id;
        try {
            id = Long.parseLong(idStr);
            logger.info("ID de actividad convertido a Long: {}", id);
        } catch (NumberFormatException e) {
            logger.error("ERROR: El ID de actividad no es un número válido: {}", idStr, e);
            return "redirect:/modulos";
        }
        
        try {
            List<Activity> siguientes = activityService.obtenerSiguientesActividades(id);
            
            if (siguientes != null && !siguientes.isEmpty()) {
                logger.info("Redirigiendo a la siguiente actividad: ID {}", siguientes.get(0).getId());
                return "redirect:/actividad/" + siguientes.get(0).getId() + 
                        (moduloId != null ? "?moduloId=" + moduloId : "");
            } else {
                logger.info("No hay más actividades siguientes, redirigiendo a módulos");
                if (moduloId != null) {
                    return "redirect:/modulo/" + moduloId;
                } else {
                    return "redirect:/modulos";
                }
            }
        } catch (Exception e) {
            logger.error("Error al obtener la siguiente actividad: {}", e.getMessage(), e);
            if (moduloId != null) {
                return "redirect:/modulo/" + moduloId;
            } else {
                return "redirect:/modulos";
            }
        }
    }
    
    @GetMapping("/actividad/{id}")
    public String cargarActividad(
            @PathVariable("id") String idStr,
            @RequestParam(name = "moduloId", required = false) Long moduloId,
            Model model, 
            CsrfToken csrfToken) {
        
        logger.info("=== INICIO: Cargando actividad con ID: {} ===", idStr);
        logger.info("ID de módulo recibido: {}", moduloId);
        
        if (idStr == null || idStr.trim().isEmpty() || "undefined".equalsIgnoreCase(idStr)) {
            logger.error("ERROR: ID de actividad no proporcionado o inválido: {}", idStr);
            return "redirect:/modulos";
        }
        
        
        Long id;
        try {
            id = Long.parseLong(idStr);
            logger.info("ID de actividad convertido a Long: {}", id);
        } catch (NumberFormatException e) {
            logger.error("ERROR: El ID de actividad no es un número válido: {}", idStr, e);
            return "redirect:/modulos";
        }
        
        try {
            
            logger.info("Buscando actividad en la base de datos...");
            Optional<Activity> actividadOpt = activityRepository.findById(id);
            
            if (actividadOpt.isEmpty()) {
                logger.error("ERROR: No se encontró la actividad con ID: {}", id);
                return "redirect:/modulos";
            }
            
            Activity actividad = actividadOpt.get();
            
            
            if ("QUIZ".equalsIgnoreCase(actividad.getTipoActividad())) {
                logger.info("Cargando opciones del quiz...");
                actividad = activityRepository.findByIdWithQuizOptions(id).orElse(actividad);
            } else if ("RELACIONAR".equalsIgnoreCase(actividad.getTipoActividad())) {
                logger.info("Cargando pares relacionados...");
                actividad = activityRepository.findByIdWithMatchingPairs(id).orElse(actividad);
            } else if (actividad.getTipoActividad() != null && 
                     (actividad.getTipoActividad().toUpperCase().contains("VIDEO") || 
                      actividad.getTipoActividad().toUpperCase().contains("PALABRAS"))) {
                logger.info("Cargando palabras del video...");
                actividad = activityRepository.findByIdWithOrderedVideoWords(id).orElse(actividad);
            } else {
                actividad = activityRepository.findByIdWithContents(id).orElse(actividad);
            }
            logger.info("Actividad encontrada - ID: {}, Título: {}", actividad.getId(), actividad.getTitulo());
            logger.info("Tipo de actividad: {}", actividad.getTipoActividad());
            
            
            if (actividad.getModulo() == null) {
                logger.error("ERROR: La actividad con ID {} no tiene un módulo asociado", id);
                return "redirect:/modulos";
            }
            
            logger.info("Módulo asociado - ID: {}, Nombre: {}", 
                actividad.getModulo().getId(), actividad.getModulo().getNombre());
            
            
            if (actividad.getContenidos() != null) {
                logger.info("Número de contenidos: {}", actividad.getContenidos().size());
            } else {
                logger.warn("La actividad no tiene contenidos asociados");
            }
            
            
            ActivityDTO actividadDTO = ActivityDTO.fromEntity(actividad);
            logger.info("DTO creado correctamente");
            logger.info("URL del video: {}", actividad.getUrlVideo());
            logger.info("URL del video en DTO: {}", actividadDTO.getUrlVideo());
            
            
            List<Activity> actividadesAnteriores = activityRepository.findPreviousActivities(id);
            List<Activity> actividadesSiguientes = activityRepository.findNextActivities(id);
            
            
            Long actividadAnteriorId = actividadesAnteriores.isEmpty() ? null : actividadesAnteriores.get(0).getId();
            Long actividadSiguienteId = actividadesSiguientes.isEmpty() ? null : actividadesSiguientes.get(0).getId();
            
            logger.info("Actividad anterior ID: {}", actividadAnteriorId);
            logger.info("Actividad siguiente ID: {}", actividadSiguienteId);
            
            
            model.addAttribute("actividad", actividadDTO);
            model.addAttribute("moduloId", actividad.getModulo().getId());
            model.addAttribute("actividadAnteriorId", actividadAnteriorId);
            model.addAttribute("actividadSiguienteId", actividadSiguienteId);
            
            
            if (csrfToken != null) {
                model.addAttribute("_csrf", csrfToken);
                logger.info("Token CSRF agregado al modelo");
            } else {
                logger.warn("No se pudo obtener el token CSRF");
            }
            
            logger.info("Atributos agregados al modelo");
            
            
            String viewName = "activity/lectura"; 
            
            try {
                if (actividad.getTipoActividad() != null) {
                    String tipoActividad = actividad.getTipoActividad().toUpperCase();
                    logger.info("Tipo de actividad: {}", tipoActividad);
                    
                    
                    switch (tipoActividad) {
                        case "LECTURA":
                        case "TEXTO":
                            viewName = "activity/lectura";
                            break;
                        case "QUIZ":
                        case "CUESTIONARIO":
                            viewName = "activity/quiz";
                            break;
                        case "RELACIONAR":
                        case "EMPAREJAR":
                            viewName = "activity/matching";
                            break;
                        case "VIDEO":
                            viewName = "activity/video"; 
                            break;
                        case "ROMPECABEZAS":
                        case "PUZZLE":
                            viewName = "activity/rompecabezas";
                            break;
                        default:
                            logger.warn("Tipo de actividad '{}' no reconocido, usando vista por defecto", tipoActividad);
                            viewName = "activity/lectura";
                    }
                    
                    logger.info("Usando plantilla: {}", viewName);
                } else {
                    logger.warn("Tipo de actividad no especificado, usando vista por defecto");
                }
            } catch (Exception e) {
                logger.error("Error al determinar la plantilla para la actividad: {}", e.getMessage());
                viewName = "activity/lectura";
            }
            
            logger.info("Redirigiendo a la vista: {}", viewName);
            return viewName;
            
        } catch (Exception e) {
            logger.error("ERROR al cargar la actividad con ID: " + id, e);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stackTrace = sw.toString();
            
            model.addAttribute("error", "Error al cargar la actividad");
            model.addAttribute("message", e.getMessage());
            model.addAttribute("status", "500");
            model.addAttribute("trace", stackTrace);
            return "error";
        }
    }

}
