package com.project.ecuy.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {
    
    @GetMapping("/")
    public String mostrarLogin(@RequestParam(value = "logout", required = false) String logout,
                             @RequestParam(value = "error", required = false) String error,
                             Model model) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            for (GrantedAuthority authority : auth.getAuthorities()) {
                if (authority.getAuthority().equals("ROLE_ADMIN")) {
                    return "redirect:/admin/dashboard";
                }
            }
            return "redirect:/modulos";
        }
        
        if (logout != null) {
            model.addAttribute("logout", "Has cerrado sesión correctamente.");
        }
        if (error != null) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
        }
        return "login";
    }
    
    @GetMapping("/login")
    public String loginPage() {
        return "redirect:/";
    }
}
