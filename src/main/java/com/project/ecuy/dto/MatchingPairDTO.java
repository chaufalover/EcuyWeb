package com.project.ecuy.dto;

import com.project.ecuy.entities.MatchingPair;

public class MatchingPairDTO {
    private Long id;
    private String urlImagen;
    private String textoDescripcion;
    private Integer orden;

    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    public String getTextoDescripcion() {
        return textoDescripcion;
    }

    public void setTextoDescripcion(String textoDescripcion) {
        this.textoDescripcion = textoDescripcion;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    
    public static MatchingPairDTO fromEntity(MatchingPair pair) {
        if (pair == null) return null;
        
        MatchingPairDTO dto = new MatchingPairDTO();
        dto.setId(pair.getId());
        dto.setUrlImagen(pair.getUrlImagen());
        dto.setTextoDescripcion(pair.getTextoDescripcion());
        dto.setOrden(pair.getOrden());
        return dto;
    }
}
