package com.project.ecuy.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "modulos")
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString(exclude = {"actividades", "progresoUsuarios"})
public class Module {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_modulo")
    @Builder.Default
    private Long id = null;

    @Column(nullable = false, unique = true)
    @Builder.Default
    private String nombre = "";

    @Column(columnDefinition = "TEXT")
    @Builder.Default
    private String descripcion = "";

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_actualizacion")
    @Builder.Default
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;

    @OneToMany(mappedBy = "modulo", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"modulo", "hibernateLazyInitializer", "handler"})
    @OrderBy("orden ASC")
    @Builder.Default
    private List<Activity> actividades = new ArrayList<>();

    @OneToMany(mappedBy = "modulo", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"modulo", "hibernateLazyInitializer", "handler"})
    @Builder.Default
    private List<UserProgress> progresoUsuarios = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        this.fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
