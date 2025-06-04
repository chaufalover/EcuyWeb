package com.project.ecuy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Completar el nombre")
    private String nombre;

    @NotBlank(message = "Completar el apellido")
    private String apellido;

    @NotBlank(message = "Ingresar correo")
    @Email(message = "Ingresar correo valido")
    private String correo;

    @NotBlank(message = "Ingresar contraseña")
    @Size(min = 8, message = "La contraseña deberia tener al menos 8 caracteres")
    private String password;

    @NotBlank(message = "Ingresar usuario")
    private String usuario;

}
