package com.project.ecuy.dto;

import com.project.ecuy.entities.Activity;

import java.util.List;
import java.util.stream.Collectors;

public class ActivityDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private String tipoActividad;
    private Integer orden;
    private String urlVideo; 
    private String urlImagenRompecabezas; 
    private List<ActivityContentDTO> contenidos;
    private List<QuizOptionDTO> opciones; 
    private List<MatchingPairDTO> paresRelacionados; 

    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipoActividad() {
        return tipoActividad;
    }

    public void setTipoActividad(String tipoActividad) {
        this.tipoActividad = tipoActividad;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public String getUrlVideo() {
        return urlVideo;
    }

    public void setUrlVideo(String urlVideo) {
        this.urlVideo = urlVideo;
    }

    public List<ActivityContentDTO> getContenidos() {
        return contenidos;
    }

    public void setContenidos(List<ActivityContentDTO> contenidos) {
        this.contenidos = contenidos;
    }
    
    public List<QuizOptionDTO> getOpciones() {
        return opciones;
    }

    public void setOpciones(List<QuizOptionDTO> opciones) {
        this.opciones = opciones;
    }
    
    
    public String getUrlImagen() {
        return this.urlImagenRompecabezas;
    }
    
    public String getUrlImagenRompecabezas() {
        return urlImagenRompecabezas;
    }

    public void setUrlImagenRompecabezas(String urlImagenRompecabezas) {
        this.urlImagenRompecabezas = urlImagenRompecabezas;
    }
    
    public List<MatchingPairDTO> getParesRelacionados() {
        return paresRelacionados;
    }

    public void setParesRelacionados(List<MatchingPairDTO> paresRelacionados) {
        this.paresRelacionados = paresRelacionados;
    }

    public static ActivityDTO fromEntity(Activity activity) {
        if (activity == null) return null;
        
        ActivityDTO dto = new ActivityDTO();
        dto.setId(activity.getId());
        dto.setTitulo(activity.getTitulo());
        dto.setDescripcion(activity.getDescripcion());
        dto.setTipoActividad(activity.getTipoActividad());
        dto.setOrden(activity.getOrden());
        
       
        if (activity.getUrlVideo() != null && !activity.getUrlVideo().isEmpty()) {
            
            if (activity.getUrlVideo().contains("youtube.com") || activity.getUrlVideo().contains("youtu.be")) {
                String videoId = extractYoutubeId(activity.getUrlVideo());
                dto.setUrlVideo(videoId);
            } else {
                
                dto.setUrlVideo(activity.getUrlVideo());
            }
        }
        
       
        dto.setUrlImagenRompecabezas(activity.getUrlImagenRompecabezas());
        
        
        if (activity.getContenidos() != null) {
            dto.setContenidos(activity.getContenidos().stream()
                .map(ActivityContentDTO::fromEntity)
                .collect(Collectors.toList()));
        }
        
        
        if (activity.getParesRelacionados() != null && !activity.getParesRelacionados().isEmpty()) {
            List<MatchingPairDTO> paresDTO = activity.getParesRelacionados().stream()
                .map(MatchingPairDTO::fromEntity)
                .collect(Collectors.toList());
            dto.setParesRelacionados(paresDTO);
        }
        
        
        if (activity.getOpcionesQuiz() != null && !activity.getOpcionesQuiz().isEmpty()) {
            List<QuizOptionDTO> opcionesDTO = activity.getOpcionesQuiz().stream()
                .map(opcion -> {
                    QuizOptionDTO opcionDto = new QuizOptionDTO();
                    opcionDto.setId(opcion.getId());
                    opcionDto.setTexto(opcion.getTexto());
                    opcionDto.setEsCorrecta(opcion.isEsCorrecta());
                    return opcionDto;
                })
                .collect(Collectors.toList());
            dto.setOpciones(opcionesDTO);
        }
        
        return dto;
    }
    
    
    private static String extractYoutubeId(String url) {
        String videoId = null;
        
        String pattern = "(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?v%3D|%2Fvideos%2F|embed%2Fvideos%2F|watch\\?feature=player_embedded&v=|%2Fv%2F)[^#&?\\n]*";
        
        java.util.regex.Pattern compiledPattern = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher matcher = compiledPattern.matcher(url);
        
        if (matcher.find()) {
            videoId = matcher.group();
        }
        
        return videoId != null ? videoId : url; 
    }
}
