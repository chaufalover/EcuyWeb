package com.project.ecuy.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.project.ecuy.util.RatingEnum;

@Entity
@Table(name = "encuestas_modulo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleSurvey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_encuesta")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    @ToString.Exclude
    private User usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_modulo", nullable = false)
    @ToString.Exclude
    private Module modulo;

    @Enumerated(EnumType.STRING)
    @Column(name = "calificacion", nullable = false)
    private RatingEnum calificacion;

    @Column(name = "comentario", columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
