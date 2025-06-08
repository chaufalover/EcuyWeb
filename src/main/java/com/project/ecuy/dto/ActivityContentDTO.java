package com.project.ecuy.dto;

import com.project.ecuy.entities.ActivityContent;
import lombok.Data;

@Data
public class ActivityContentDTO {
    private Long id;
    private String contenido;
    private String tipoContenido;
    private String urlArchivo;
    private Integer orden;

    public static ActivityContentDTO fromEntity(ActivityContent content) {
        if (content == null) return null;
        
        ActivityContentDTO dto = new ActivityContentDTO();
        dto.setId(content.getId());
        dto.setContenido(content.getContenido());
        dto.setTipoContenido(content.getTipoContenido());
        dto.setUrlArchivo(content.getUrlArchivo());
        dto.setOrden(content.getOrden());
        
        return dto;
    }
}
