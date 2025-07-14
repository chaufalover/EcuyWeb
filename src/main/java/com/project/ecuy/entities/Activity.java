package com.project.ecuy.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "actividades")
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString(exclude = { "modulo", "contenidos", "opcionesQuiz", "paresRelacionados", "palabrasVideo" })
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_actividad")
    @Builder.Default
    private Long id = null;

    @Column(nullable = false)
    @Builder.Default
    private String titulo = "";

    @Column(columnDefinition = "TEXT")
    @Builder.Default
    private String descripcion = "";

    @Column(name = "tipo_actividad", nullable = false, length = 50)
    @Builder.Default
    private String tipoActividad = "";

    @Column(name = "orden", nullable = false)
    @Builder.Default
    private Integer orden = 0;

    @Column(name = "puntos")
    @Builder.Default
    private Integer puntos = 0;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;

    @Column(name = "url_video", length = 255)
    @Builder.Default
    private String urlVideo = null;

    @Column(name = "url_imagen_rompecabezas", length = 255)
    @Builder.Default
    private String urlImagenRompecabezas = null;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_modulo", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Module modulo;

    @OneToMany(mappedBy = "actividad", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("actividad")
    @Builder.Default
    private List<ActivityContent> contenidos = new ArrayList<>();

    @OneToMany(mappedBy = "actividad", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("actividad")
    @Builder.Default
    private List<QuizOption> opcionesQuiz = new ArrayList<>();

    @OneToMany(mappedBy = "actividad", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({ "actividad", "hibernateLazyInitializer", "handler" })
    @Builder.Default
    private List<MatchingPair> paresRelacionados = new ArrayList<>();

    @OneToMany(mappedBy = "actividad", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({ "actividad", "hibernateLazyInitializer", "handler" })
    @OrderBy("ordenCorrecto ASC")
    @Builder.Default
    private List<VideoWord> palabrasVideo = new ArrayList<>();
}
