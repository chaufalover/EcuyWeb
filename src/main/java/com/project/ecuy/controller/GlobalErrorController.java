package com.project.ecuy.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class GlobalErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("errorCode", "404");
                model.addAttribute("errorTitle", "Página no encontrada");
                model.addAttribute("errorMessage", "La página que estás buscando no existe o ha sido movida.");
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                model.addAttribute("errorCode", "500");
                model.addAttribute("errorTitle", "Error del servidor");
                model.addAttribute("errorMessage", "¡Vaya! Algo salió mal en nuestro servidor. Por favor, inténtalo de nuevo más tarde.");
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("errorCode", "403");
                model.addAttribute("errorTitle", "Acceso denegado");
                model.addAttribute("errorMessage", "No tienes permiso para acceder a este recurso.");
            } else {
                model.addAttribute("errorCode", statusCode);
                model.addAttribute("errorTitle", "Error");
                model.addAttribute("errorMessage", "Ha ocurrido un error inesperado.");
            }
        } else {
            model.addAttribute("errorCode", "Error");
            model.addAttribute("errorTitle", "Error");
            model.addAttribute("errorMessage", "Ha ocurrido un error inesperado.");
        }
        
        return "error";
    }
}
