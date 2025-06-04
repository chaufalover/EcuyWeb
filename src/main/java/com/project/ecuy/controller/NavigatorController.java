package com.project.ecuy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class NavigatorController {
    
    @GetMapping
    public String login(){
        return "login";
    }

    @GetMapping("/modulos")
    public String modules(){
        return "modules";
    }

    @GetMapping("/capitulo")
    public String chapter(){
        return "chapters";
    }

    @GetMapping("/perfil")
    public String profile(){
        return "profile";
    }

}
