package com.project.ecuy.services;

import com.project.ecuy.repository.StatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final StatsRepository statsRepository;
    private static final int RECENT_ACTIVITY_LIMIT = 10;

    @Autowired
    public StatsService(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }
    
    
    public Map<String, Object> getUserProgressByModule(Long moduloId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            
            Map<String, Integer> progressData = statsRepository.getUserProgressByModule(moduloId);
            
            
            var modulos = statsRepository.getProgressByModule();
            String nombreModulo = modulos.values().stream()
                .filter(m -> ((Number) m.get("id")).longValue() == moduloId.longValue())
                .map(m -> (String) m.get("nombre"))
                .findFirst()
                .orElse("Módulo " + moduloId);
            
            
            Map<String, Integer> formattedData = new HashMap<>();
            progressData.forEach((key, value) -> {
                String formattedKey = key.toUpperCase().replace(" ", "_");
                formattedData.put(formattedKey, value);
            });
            
            result.put("moduloId", moduloId);
            result.put("nombreModulo", nombreModulo);
            result.put("progressData", formattedData);
            
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("moduloId", moduloId);
            errorResult.put("nombreModulo", "Módulo " + moduloId);
            errorResult.put("progressData", Map.of(
                "NO_INICIADO", 0,
                "EN_PROGRESO", 0,
                "COMPLETADO", 0
            ));
            return errorResult;
        }
    }
    
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        
        LocalDate sixMonthsAgo = YearMonth.now().minusMonths(6).atDay(1);
        var usersByMonth = statsRepository.countUsersByMonth(sixMonthsAgo);
        
        
        Map<String, Integer> usersChartData = new LinkedHashMap<>();
        Locale spanishLocale = Locale.forLanguageTag("es-ES");
        
        
        for (int i = 5; i >= 0; i--) {
            YearMonth month = YearMonth.now().minusMonths(i);
            String monthName = month.getMonth().getDisplayName(TextStyle.SHORT, spanishLocale);
            usersChartData.put(monthName, 0);
        }
        
        
        usersByMonth.forEach(row -> {
            int month = ((Number) row.get("mes")).intValue();
            int count = ((Number) row.get("total")).intValue();
            String monthName = java.time.Month.of(month).getDisplayName(TextStyle.SHORT, spanishLocale);
            usersChartData.put(monthName, count);
        });
        
        
        var progressStats = statsRepository.getUserProgressStats();
        
        
        var progressByModule = statsRepository.getProgressByModule();
        Map<String, Integer> progressData = new HashMap<>();
        progressStats.forEach(stat -> {
            String estado = (String) stat.get("estado");
            int total = ((Number) stat.get("total")).intValue();
            progressData.put(estado, total);
        });
        
        
        var recentActivity = statsRepository.getRecentActivity(RECENT_ACTIVITY_LIMIT).stream()
            .map(row -> {
                Map<String, String> activity = new HashMap<>();
                activity.put("usuario", (String) row.get("usuario"));
                activity.put("actividad", (String) row.get("actividad"));
                activity.put("modulo", (String) row.get("modulo"));
                activity.put("fecha", (String) row.get("fecha"));
                return activity;
            })
            .collect(Collectors.toList());
        
        
        var totalUsersResult = statsRepository.countTotalUsers();
        long totalUsers = !totalUsersResult.isEmpty() ? 
            ((Number) totalUsersResult.get(0).get("total")).longValue() : 0;
        
        
        stats.put("usersByMonth", usersChartData);
        stats.put("progressData", progressData);
        stats.put("progressByModule", progressByModule);
        stats.put("recentActivity", recentActivity);
        stats.put("totalUsers", totalUsers);
        
        return stats;
    }
}
