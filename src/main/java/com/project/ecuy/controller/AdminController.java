package com.project.ecuy.controller;

import com.project.ecuy.services.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final StatsService statsService;

    @Autowired
    public AdminController(StatsService statsService) {
        this.statsService = statsService;
    }

    private static final String DASHBOARD_VIEW = "admin/dashboard";
    private static final String ERROR_LOADING_STATS = "No se pudieron cargar las estadísticas del dashboard";
    
    /**
     * Maneja la solicitud GET para la página de dashboard del administrador.
     * 
     * @param model El modelo para pasar datos a la vista
     * @return La vista del dashboard o redirección a página de error
     */
    @GetMapping(value = {"", "/", "/dashboard"})
    @PreAuthorize("hasRole('ADMIN')")
    public String dashboard(Model model) {
        try {
            Map<String, Object> stats = statsService.getDashboardStats();
            
            if (stats == null || stats.isEmpty()) {
                throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, 
                    ERROR_LOADING_STATS
                );
            }
            
            addStatsToModel(model, stats);
            return DASHBOARD_VIEW;
            
        } catch (ResponseStatusException rse) {
            throw rse; 
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error inesperado al cargar el dashboard: " + e.getMessage(), 
                e
            );
        }
    }
    
    /**
     * Agrega las estadísticas al modelo de forma segura, manejando posibles valores nulos.
     * 
     * @param model El modelo al que se agregarán los atributos
     * @param stats Mapa de estadísticas a agregar al modelo
     */
    private void addStatsToModel(Model model, Map<String, Object> stats) {
        if (model == null || stats == null) {
            throw new IllegalArgumentException("Modelo y estadísticas no pueden ser nulos");
        }
        
        
        safeAddAttribute(model, "usuariosPorMes", stats.get("usersByMonth"));
        safeAddAttribute(model, "progresoUsuarios", stats.get("progressData"));
        safeAddAttribute(model, "progresoPorModulo", stats.get("progressByModule"));
        safeAddAttribute(model, "recentActivity", stats.get("recentActivity"));
        
        
        handleUserStats(model, stats);
        
        
        handleModuleList(model, stats);
    }
    
    
    private void handleUserStats(Model model, Map<String, Object> stats) {
        Object totalUsers = stats.get("totalUsers");
        int total = 0;
        
        if (totalUsers instanceof Number) {
            total = ((Number) totalUsers).intValue();
        }
        
        model.addAttribute("totalUsers", total);
    }
    
    
    @SuppressWarnings("unchecked")
    private void handleModuleList(Model model, Map<String, Object> stats) {
        try {
            Object progressByModule = stats.get("progressByModule");
            List<Map<String, Object>> modulos = List.of();
            
            if (progressByModule instanceof Map) {
                modulos = ((Map<String, Map<String, Object>>) progressByModule)
                    .values().stream()
                    .map(this::createModuleInfo)
                    .sorted(Comparator.comparing(m -> ((Number) m.get("id")).longValue()))
                    .collect(Collectors.toList());
            }
            
            model.addAttribute("modulos", modulos);
        } catch (Exception e) {
            model.addAttribute("modulos", List.of());
            
            System.err.println("Error al procesar la lista de módulos: " + e.getMessage());
        }
    }
    
    
    private Map<String, Object> createModuleInfo(Map<String, Object> moduleData) {
        Object id = moduleData.getOrDefault("id", 0);
        String nombre = moduleData.getOrDefault("nombre", "Módulo sin nombre").toString();
        
        
        String nombreLimpio = nombre;
        String prefijoEsperado = "Módulo " + id;
        
        
        if (nombre.startsWith(prefijoEsperado + ": ") || nombre.startsWith(prefijoEsperado + " - ") || 
            nombre.startsWith(prefijoEsperado + ":") || nombre.startsWith(prefijoEsperado + "-")) {
            nombreLimpio = nombre.substring(nombre.indexOf(" ", nombre.indexOf(prefijoEsperado) + prefijoEsperado.length())).trim();
        }
        
        else if (nombre.equals(prefijoEsperado)) {
            nombreLimpio = nombre;
        }
        
        return Map.of(
            "id", id,
            "nombre", nombreLimpio
        );
    }
    
    
    private void safeAddAttribute(Model model, String attributeName, Object value) {
        model.addAttribute(attributeName, value != null ? value : getDefaultValue(attributeName));
    }
    
    
    private Object getDefaultValue(String attributeName) {
        if (attributeName == null) return null;
        
        return switch (attributeName) {
            case "usuariosPorMes" -> List.of();
            case "progresoUsuarios", "progresoPorModulo" -> Map.of();
            case "recentActivity" -> List.of();
            default -> null;
        };
    }
}
