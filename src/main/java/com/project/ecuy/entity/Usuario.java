package com.project.ecuy.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Usuario {
    private int idUsuario;
    private String nombre;
    private String apellido;
    private String correo;
    private String password;
    private Date fecha_nac;
    private int edad;
    private String usuario;
}
