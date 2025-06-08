package com.project.ecuy.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.project.ecuy.entities.User;
import com.project.ecuy.services.UserService;

@Controller
@RequestMapping("/perfil")
public class ProfileController {
     @Autowired
    private UserService userService;

    @GetMapping
    public String verPerfil(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/";
        }

        String username = principal.getName(); 
        User usuario = userService.buscarUsuario(username);
        model.addAttribute("usuario", usuario);
        return "profile"; 
    }
}
