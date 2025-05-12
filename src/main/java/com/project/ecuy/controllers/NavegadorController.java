package com.project.ecuy.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequestMapping
public class NavegadorController {
    @GetMapping
    public String index() {
        return "index";
    }
    @GetMapping("/recuperacion")
    public String recuperarContra() {
        return "recuperar-contra";
    }

    @GetMapping("/nueva-contra")
    public String nuevaContra() {
        return "nueva-contra";
    }

    @GetMapping("/inicio")
    public String inicio() {
        return "principal";
    }

    @GetMapping("/registro")
    public String registro() {
        return "registro";
    }
}
