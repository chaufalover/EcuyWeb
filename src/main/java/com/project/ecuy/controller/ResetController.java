package com.project.ecuy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.ecuy.services.UserService;


@Controller
public class ResetController {
   private final UserService userService;

    public ResetController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/reset-password")
    public String mostrarFormulario(@RequestParam String token, org.springframework.ui.Model model) {
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String procesarCambio(@RequestParam String token, @RequestParam String password, RedirectAttributes redirect) {
        try {
            userService.restablecerPasswordConToken(token, password);
            redirect.addFlashAttribute("mensaje", "Contraseña actualizada correctamente.");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("error", "Token inválido o expirado.");
        }
        return "redirect:/";
    }
}
