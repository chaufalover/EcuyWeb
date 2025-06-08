package com.project.ecuy.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "palabras_video")
@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoWord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_palabra")
    private Long id;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String texto;
    
    @Column(name = "orden_correcto", nullable = false)
    private Integer ordenCorrecto;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_actividad", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Activity actividad;
    
    public VideoWord(String texto, int ordenCorrecto) {
        this.texto = texto;
        this.ordenCorrecto = ordenCorrecto;
    }
}
