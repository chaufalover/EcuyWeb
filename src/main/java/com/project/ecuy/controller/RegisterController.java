package com.project.ecuy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.ecuy.dto.RegisterRequest;
import com.project.ecuy.services.UserService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/registro")
public class RegisterController {

    private UserService servicio;

    public RegisterController(UserService servicio) {
        this.servicio = servicio;
    }

    @ModelAttribute("usuario")
    public RegisterRequest retornarUsuario() {
        return new RegisterRequest();
    }

    @GetMapping
    public String mostrarRegistro() {
        return "register";
    }

    @PostMapping
    public String registrarCuenta(@Valid @ModelAttribute("usuario") RegisterRequest registroDTO, BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "register";
        }

        try {
            servicio.registrar(registroDTO);
            redirectAttributes.addFlashAttribute("exito", "¡Registro exitoso! Ahora puedes iniciar sesión.");
            return "redirect:/";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/registro";
        }
    }

}
