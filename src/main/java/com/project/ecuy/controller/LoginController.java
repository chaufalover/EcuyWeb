package com.project.ecuy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/")
public class LoginController {
    @GetMapping("/")
public String mostrarLogin(@RequestParam(value = "logout", required = false) String logout,
                           Model model) {
    if (logout != null) {
        model.addAttribute("logout", "Has cerrado sesión correctamente.");
    }
    return "login";
}

}
