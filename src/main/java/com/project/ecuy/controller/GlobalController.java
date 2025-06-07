package com.project.ecuy.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.project.ecuy.entities.User;

import jakarta.servlet.http.HttpSession;

@ControllerAdvice
public class GlobalController {
    @ModelAttribute("usuarioLogueado")
    public User usuarioSesion(HttpSession session) {
        return (User) session.getAttribute("usuarioLogueado");
    }
}
