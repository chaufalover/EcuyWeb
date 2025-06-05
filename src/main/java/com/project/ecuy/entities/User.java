package com.project.ecuy.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer id;

    private String nombre;

    private String apellido;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String usuario;

    @Column(nullable = false)
    private String rol;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "token_expiracion")
    private LocalDateTime tokenExpiration;

    public User(String nombre, String apellido, String correo, String password, String rol, String usuario) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.password = password;
        this.rol = rol;
        this.usuario = usuario;

    }

}
