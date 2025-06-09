package com.project.ecuy.repository;

import com.project.ecuy.entities.User;
import com.project.ecuy.entities.UserActivityProgress;
import com.project.ecuy.entities.Module;
import com.project.ecuy.entities.Activity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class StatsRepository {
    
    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final UserActivityProgressRepository userActivityProgressRepository;
    private final ActivityRepository activityRepository;
    
    public StatsRepository(JdbcTemplate jdbcTemplate, 
                          UserRepository userRepository,
                          UserActivityProgressRepository userActivityProgressRepository,
                          ActivityRepository activityRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRepository = userRepository;
        this.userActivityProgressRepository = userActivityProgressRepository;
        this.activityRepository = activityRepository;
    }
    
    
    public List<Map<String, Object>> countUsersByMonth(LocalDate fechaInicio) {
        try {
            
            List<User> usuarios = userRepository.findAll();
            
            
            Map<Integer, Long> usuariosPorMes = usuarios.stream()
                .filter(u -> u.getTokenExpiration() != null && !u.getTokenExpiration().isBefore(fechaInicio.atStartOfDay()))
                .collect(Collectors.groupingBy(
                    u -> u.getTokenExpiration().getMonthValue(),
                    TreeMap::new,
                    Collectors.counting()
                ));
            
            
            List<Map<String, Object>> resultado = new ArrayList<>();
            YearMonth ahora = YearMonth.now();
            for (int i = 5; i >= 0; i--) {
                YearMonth mes = ahora.minusMonths(i);
                int mesNumero = mes.getMonthValue();
                long total = usuariosPorMes.getOrDefault(mesNumero, 0L);
                resultado.add(Map.of("mes", mesNumero, "total", total));
            }
            
            return resultado;
            
        } catch (Exception e) {
            e.printStackTrace();
            
            return List.of(
                Map.of("mes", 1, "total", 0),
                Map.of("mes", 2, "total", 0),
                Map.of("mes", 3, "total", 0),
                Map.of("mes", 4, "total", 0),
                Map.of("mes", 5, "total", 0),
                Map.of("mes", 6, "total", 0)
            );
        }
    }
    
    
    public List<Map<String, Object>> getUserProgressStats() {
        try {
            
            List<User> usuarios = userRepository.findAll();
            
            
            List<UserActivityProgress> progresos = userActivityProgressRepository.findByCompletedAtIsNotNull();
            
            
            Map<User, Map<com.project.ecuy.entities.Module, Long>> progresoPorUsuarioYModulo = new HashMap<>();
            
            for (UserActivityProgress progreso : progresos) {
                if (progreso.getActivity() == null || progreso.getActivity().getModulo() == null) {
                    continue;
                }
                
                User usuario = progreso.getUser();
                com.project.ecuy.entities.Module modulo = progreso.getActivity().getModulo();
                
                
                Map<com.project.ecuy.entities.Module, Long> modulosUsuario = progresoPorUsuarioYModulo
                    .computeIfAbsent(usuario, k -> new HashMap<>());
                
                
                modulosUsuario.merge(modulo, 1L, Long::sum);
            }
            
            
            long totalModulos = progresos.stream()
                .map(p -> p.getActivity() != null ? p.getActivity().getModulo() : null)
                .filter(Objects::nonNull)
                .distinct()
                .count();
                
            
            if (totalModulos == 0) {
                return List.of(
                    Map.of("estado", "No iniciado", "total", usuarios.size()),
                    Map.of("estado", "En progreso", "total", 0),
                    Map.of("estado", "Completado", "total", 0)
                );
            }
            
            
            int noIniciado = 0;
            int enProgreso = 0;
            int completado = 0;
            
            for (User usuario : usuarios) {
                Map<com.project.ecuy.entities.Module, Long> modulosCompletados = 
                    progresoPorUsuarioYModulo.getOrDefault(usuario, Collections.emptyMap());
                long modulosCompletadosCount = modulosCompletados.entrySet().stream()
                    .filter(entry -> {
                        
                        return entry.getValue() > 0;
                    })
                    .count();
                
                double porcentajeCompletado = (modulosCompletadosCount * 100.0) / totalModulos;
                
                if (porcentajeCompletado == 0) {
                    noIniciado++;
                } else if (porcentajeCompletado >= 100) {
                    completado++;
                } else {
                    enProgreso++;
                }
            }
            
            return List.of(
                Map.of("estado", "No iniciado", "total", noIniciado),
                Map.of("estado", "En progreso", "total", enProgreso),
                Map.of("estado", "Completado", "total", completado)
            );
            
        } catch (Exception e) {
            e.printStackTrace();
            
            return List.of(
                Map.of("estado", "No iniciado", "total", 0),
                Map.of("estado", "En progreso", "total", 0),
                Map.of("estado", "Completado", "total", 0)
            );
        }
    }
    
    
    public List<Map<String, Object>> getRecentActivity(int limit) {
        try {
            
            List<UserActivityProgress> actividadesRecientes = userActivityProgressRepository
                .findByCompletedAtIsNotNullOrderByCompletedAtDesc();
            
            
            return actividadesRecientes.stream()
                .limit(limit)
                .map(actividad -> {
                    User usuario = actividad.getUser();
                    String nombreUsuario = usuario != null ? usuario.getUsuario() : "Usuario desconocido";
                    String nombreActividad = actividad.getActivity() != null ? 
                        actividad.getActivity().getTitulo() : "Actividad desconocida";
                    
                    
                    String modulo = "Módulo desconocido";
                    if (actividad.getActivity() != null && actividad.getActivity().getModulo() != null) {
                        modulo = actividad.getActivity().getModulo().getNombre();
                    }
                    
                    String fecha = actividad.getCompletedAt() != null ?
                        actividad.getCompletedAt().toString() : "Fecha desconocida";
                    
                    Map<String, Object> actividadMap = new HashMap<>();
                    actividadMap.put("usuario", nombreUsuario);
                    actividadMap.put("actividad", "Completó la actividad: " + nombreActividad);
                    actividadMap.put("modulo", modulo);
                    actividadMap.put("fecha", fecha);
                    
                    return actividadMap;
                })
                .collect(Collectors.toList());
            
        } catch (Exception e) {
            e.printStackTrace();
            
            return new ArrayList<>();
        }
    }
    
    
    public List<Map<String, Object>> countTotalUsers() {
        try {
            long total = userRepository.count();
            return List.of(Map.of("total", total));
        } catch (Exception e) {
            e.printStackTrace();
            return List.of(Map.of("total", 0));
        }
    }
    
    
    public Map<String, Map<String, Object>> getProgressByModule() {
        try {
            
            List<UserActivityProgress> progresos = userActivityProgressRepository.findByCompletedAtIsNotNull();
            
            
            Map<com.project.ecuy.entities.Module, List<UserActivityProgress>> progresoPorModulo = progresos.stream()
                .filter(p -> p.getActivity() != null && p.getActivity().getModulo() != null)
                .collect(Collectors.groupingBy(p -> p.getActivity().getModulo()));
            
            
            Map<String, Map<String, Object>> resultado = new HashMap<>();
            
            progresoPorModulo.forEach((modulo, actividades) -> {
                
                long usuariosUnicos = actividades.stream()
                    .map(UserActivityProgress::getUser)
                    .distinct()
                    .count();
                
                
                long actividadesUnicas = actividades.stream()
                    .map(UserActivityProgress::getActivity)
                    .distinct()
                    .count();
                
                
                long totalUsuarios = userRepository.count();
                double porcentajeCompletado = totalUsuarios > 0 ? 
                    (usuariosUnicos * 100.0) / totalUsuarios : 0;
                
                
                Map<String, Object> stats = new HashMap<>();
                stats.put("id", modulo.getId());
                stats.put("nombre", modulo.getNombre());
                stats.put("usuariosCompletados", usuariosUnicos);
                stats.put("totalUsuarios", totalUsuarios);
                stats.put("porcentajeCompletado", Math.round(porcentajeCompletado * 100.0) / 100.0);
                stats.put("totalActividades", actividadesUnicas);
                
                resultado.put(modulo.getNombre(), stats);
            });
            
            return resultado;
            
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }
    
    
    public Map<String, Integer> getUserProgressByModule(Long moduloId) {
        try {
            
            List<User> usuarios = userRepository.findAll();
            
            
            List<Activity> actividades = activityRepository.findByModuloId(moduloId);
            
            if (actividades.isEmpty()) {
                return Map.of("No iniciado", 0, "En progreso", 0, "Completado", 0);
            }
            
            
            int noIniciado = 0;
            int enProgreso = 0;
            int completado = 0;
            
            for (User usuario : usuarios) {
                
                List<UserActivityProgress> progresos = userActivityProgressRepository
                    .findByUserAndActivityIn(usuario, actividades);
                
                if (progresos.isEmpty()) {
                    noIniciado++;
                    continue;
                }
                
                
                boolean todasCompletadas = progresos.stream()
                    .allMatch(progreso -> progreso.getCompletedAt() != null);
                    
                if (todasCompletadas) {
                    completado++;
                } else {
                    enProgreso++;
                }
            }
            
            return Map.of(
                "No iniciado", noIniciado,
                "En progreso", enProgreso,
                "Completado", completado
            );
            
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("Error", 0);
        }
    }
}
