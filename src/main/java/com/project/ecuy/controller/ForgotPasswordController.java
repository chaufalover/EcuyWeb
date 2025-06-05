package com.project.ecuy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.ecuy.services.PasswordService;

@Controller
public class ForgotPasswordController {
    private final PasswordService resetService;

    public ForgotPasswordController(PasswordService resetService) {
        this.resetService = resetService;
    }

    @GetMapping("/forgot-password")
    public String mostrarFormulario() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String procesarFormulario(@RequestParam String correo, RedirectAttributes redirect) {
        try {
            resetService.enviarLinkRecuperacion(correo);
            redirect.addFlashAttribute("mensaje", "Revisa tu correo para restablecer la contraseña.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Correo no válido.");
        }
        return "redirect:/";
    }
    
}
