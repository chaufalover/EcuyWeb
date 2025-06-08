package com.project.ecuy.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "contenidos_actividad")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActivityContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contenido")
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "tipo_contenido", length = 50)
    private String tipoContenido; 

    @Column(name = "url_archivo")
    private String urlArchivo;

    @Column(name = "orden")
    private Integer orden = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_actividad", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Activity actividad;
}
