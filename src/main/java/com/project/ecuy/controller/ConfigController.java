package com.project.ecuy.controller;

import java.security.Principal;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.ecuy.entities.User;
import com.project.ecuy.services.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/configuracion")
@RequiredArgsConstructor
public class ConfigController {

    private final UserService userService;

    @GetMapping
    public String mostrarConfiguracion(Model model, Principal principal) {
         if (principal == null) {
            return "redirect:/";
        }

        String username = principal.getName(); 
        User usuario = userService.buscarUsuario(username);
        model.addAttribute("usuario", usuario);
        return "configuration";
    }

    @PostMapping("/cambiar-usuario")
    public String cambiarNombreUsuario(
            @RequestParam("nuevoUsername") String nuevoUsername,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        String actualUsername = userDetails.getUsername();

        try {
            userService.cambiarNombreUsuario(actualUsername, nuevoUsername);
            redirectAttributes.addFlashAttribute("exito", "Nombre de usuario actualizado correctamente.");
            return "redirect:/configuracion";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/configuracion";
        }
    }

    @PostMapping("/cambiar-correo")
    public String cambiarCorreo(
            @RequestParam("nuevoCorreo") String nuevoCorreo,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        String actualUsername = userDetails.getUsername();

        try {
            userService.cambiarCorreo(actualUsername, nuevoCorreo);
            redirectAttributes.addFlashAttribute("exito", "Correo actualizado correctamente.");
            return "redirect:/configuracion";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/configuracion";
        }
    }
}
