package com.project.ecuy.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "opciones_quiz")
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuizOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_opcion")
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String texto;

    @Column(name = "es_correcta", nullable = false)
    private boolean esCorrecta = false;

    @Column(name = "orden")
    private Integer orden = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_actividad", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Activity actividad;
}
