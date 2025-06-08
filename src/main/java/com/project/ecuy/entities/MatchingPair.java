package com.project.ecuy.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "pares_relacionados")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchingPair {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_par")
    private Long id;

    @Column(name = "url_imagen")
    private String urlImagen;

    @Column(name = "texto_descripcion", columnDefinition = "TEXT")
    private String textoDescripcion;

    @Column(name = "orden")
    private Integer orden = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_actividad", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Activity actividad;
}
