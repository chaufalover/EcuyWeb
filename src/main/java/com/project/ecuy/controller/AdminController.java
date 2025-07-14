package com.project.ecuy.controller;

import com.project.ecuy.entities.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.ecuy.entities.Activity;
import com.project.ecuy.entities.Module;
import com.project.ecuy.entities.ModuleSurvey;
import com.project.ecuy.services.ActivityService;
import com.project.ecuy.services.ModuleService;
import com.project.ecuy.services.ModuleSurveyService;
import com.project.ecuy.services.StatsService;
import com.project.ecuy.services.UserService;
import lombok.RequiredArgsConstructor;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("admin")
@RequiredArgsConstructor
public class AdminController {
    private final ModuleSurveyService surveyService;
    private final UserService service;
    private final ModuleService mservice;
    private final ActivityService activityService;

    private final StatsService statsService;

    @GetMapping("usuarios")
    public String listaUsuarios(@RequestParam(name = "busqueda", required = false) String busqueda, Model model)
            throws JsonProcessingException {

        List<User> usuarios = (busqueda != null && !busqueda.isBlank()) ? service.buscarUsuarios(busqueda)
                : service.selectAll();

        model.addAttribute("lista", usuarios);
        model.addAttribute("usuario", new User());
        model.addAttribute("totalUsers", usuarios.size());
        model.addAttribute("param", busqueda);

        Map<String, Object> dashboardStats = statsService.getDashboardStats();

        model.addAttribute("usersByMonth", dashboardStats.get("usersByMonth"));
        model.addAttribute("progressData", dashboardStats.get("progressData"));
        model.addAttribute("progressByModule", dashboardStats.get("progressByModule"));
        model.addAttribute("recentActivity", dashboardStats.get("recentActivity"));
        model.addAttribute("totalUsers", dashboardStats.get("totalUsers"));

        model.addAttribute("progresoUsuarios", dashboardStats.get("progressData"));
        ObjectMapper mapper = new ObjectMapper();
        String progresoUsuariosJson = mapper.writeValueAsString(dashboardStats.get("progressData"));
        model.addAttribute("progresoUsuariosJson", progresoUsuariosJson);

        return "/admin/report-user";
    }

    @GetMapping("dashboard")
    public String dashboard(Model model) throws JsonProcessingException {
        int total = service.totalUsuarios();
        model.addAttribute("totalUsers", total);
        int totalm = mservice.totalModulos();
        model.addAttribute("totalModules", totalm);
        Map<String, Object> dashboardStats = statsService.getDashboardStats();

        model.addAttribute("usersByMonth", dashboardStats.get("usersByMonth"));
        model.addAttribute("progressData", dashboardStats.get("progressData"));
        model.addAttribute("progressByModule", dashboardStats.get("progressByModule"));
        model.addAttribute("recentActivity", dashboardStats.get("recentActivity"));
        model.addAttribute("totalUsers", dashboardStats.get("totalUsers"));

        model.addAttribute("progresoUsuarios", dashboardStats.get("progressData"));
        ObjectMapper mapper = new ObjectMapper();
        String progresoUsuariosJson = mapper.writeValueAsString(dashboardStats.get("progressData"));
        model.addAttribute("progresoUsuariosJson", progresoUsuariosJson);

        Long moduleId = 1L;
        List<Object[]> resumen = surveyService.obtenerResumenPorCalificacion(moduleId);

        Map<String, Long> encuestaData = new LinkedHashMap<>();
        for (Object[] fila : resumen) {
            String calificacion = String.valueOf(fila[0]);
            Long cantidad = ((Number) fila[1]).longValue();
            encuestaData.put(calificacion, cantidad);
        }

        try {
            String json = new ObjectMapper().writeValueAsString(encuestaData);
            model.addAttribute("encuestaDataJson", json);
        } catch (JsonProcessingException e) {
            model.addAttribute("encuestaDataJson", "{}");
        }

        return "/admin/dashboard";
    }

    @GetMapping("modulos")
    public String modulos(Model model) {
        List<Module> modulos = mservice.listarModulosActivos();
        int totalm = modulos.size();
        model.addAttribute("modulos", modulos);
        model.addAttribute("totalModules", totalm);
        Long moduleId = 1L;
        List<Object[]> resumen = surveyService.obtenerResumenPorCalificacion(moduleId);

        Map<String, Long> encuestaData = new LinkedHashMap<>();
        for (Object[] fila : resumen) {
            String calificacion = String.valueOf(fila[0]);
            Long cantidad = ((Number) fila[1]).longValue();
            encuestaData.put(calificacion, cantidad);
        }

        try {
            String json = new ObjectMapper().writeValueAsString(encuestaData);
            model.addAttribute("encuestaDataJson", json);
        } catch (JsonProcessingException e) {
            model.addAttribute("encuestaDataJson", "{}");
        }
        List<ModuleSurvey> encuestas = surveyService.obtenerEncuestas();
        model.addAttribute("encuestas", encuestas);

        return "/admin/modules-admin";
    }

}
