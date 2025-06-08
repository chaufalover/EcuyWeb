package com.project.ecuy.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "progreso_usuarios")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_progreso")
    private Long id;

    @Column(name = "puntuacion", nullable = false)
    private Integer puntuacion = 0;

    @Column(name = "completado", nullable = false)
    private boolean completado = false;

    @Column(name = "fecha_inicio", nullable = false, updatable = false)
    private LocalDateTime fechaInicio = LocalDateTime.now();

    @Column(name = "fecha_ultimo_intento")
    private LocalDateTime fechaUltimoIntento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_modulo", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Module modulo;

    @PrePersist
    protected void onCreate() {
        this.fechaInicio = LocalDateTime.now();
        this.fechaUltimoIntento = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaUltimoIntento = LocalDateTime.now();
    }
}
