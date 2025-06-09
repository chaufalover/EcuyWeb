package com.project.ecuy.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.project.ecuy.config.SightEngineConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
public class ImageModerationService {

    @Autowired
    private SightEngineConfig sightEngineConfig;

    @Autowired
    private RestTemplate restTemplate;

    public Map<String, Object> checkImage(MultipartFile file) throws IOException {
        Map<String, Object> result = new HashMap<>();
        
        try {
            
            if (file == null || file.isEmpty()) {
                result.put("success", false);
                result.put("message", "El archivo está vacío o no se proporcionó");
                return result;
            }
            
            
            String url = sightEngineConfig.getUrl();
            
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("media", file.getResource());
            
            
            String models = "nudity-2.0,wad,offensive,scam";
            System.out.println("Usando modelos de moderación: " + models);
            body.add("models", models);
            
            
            body.add("nudity_type", "2"); 
            body.add("offensive_type", "1"); 
            
            
            body.add("wad_min_size", "0.01"); 
            body.add("api_user", sightEngineConfig.getKey());
            body.add("api_secret", sightEngineConfig.getSecret());
            
            
            body.add("list_models", "1"); 
            
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            
            System.out.println("=== Enviando solicitud a la API de SightEngine ===");
            System.out.println("URL: " + url);
            System.out.println("Headers: " + headers);
            System.out.println("Body keys: " + body.keySet());
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                requestEntity, 
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            
            System.out.println("=== Respuesta recibida ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Headers: " + response.getHeaders());
            
            if (response.getBody() != null) {
                System.out.println("Respuesta completa de la API:");
                for (Map.Entry<String, Object> entry : response.getBody().entrySet()) {
                    System.out.println(entry.getKey() + ": " + entry.getValue());
                }
            }
            
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                System.out.println("Cuerpo de la respuesta: " + responseBody);
                
                if (responseBody == null) {
                    result.put("success", false);
                    result.put("message", "La API no devolvió datos");
                    return result;
                }
                
                if (responseBody.containsKey("error")) {
                    result.put("success", false);
                    result.put("message", "Error en la API de moderación: " + responseBody.get("error"));
                    return result;
                }
                
                
                System.out.println("=== Validando contenido de la imagen ===");
                boolean isAppropriate = isImageAppropriate(responseBody);
                System.out.println("Resultado de validación: " + (isAppropriate ? "APROBADA" : "RECHAZADA"));
                
                result.put("success", true);
                result.put("isAppropriate", isAppropriate);
                result.put("rawResponse", responseBody); 
                
                if (isAppropriate) {
                    System.out.println("La imagen no contiene contenido inapropiado según los filtros.");
                }
                
                if (!isAppropriate) {
                    String rejectionReasons = getRejectionReasons(responseBody);
                    String userFriendlyMessage = "La imagen contiene contenido inapropiado: " + rejectionReasons;
                    
                    
                    System.out.println("Imagen rechazada. Razones: " + rejectionReasons);
                    System.out.println("Respuesta completa: " + responseBody);
                    
                    
                    result.put("success", false);
                    result.put("message", userFriendlyMessage);
                    result.put("reasons", rejectionReasons);
                    result.put("isAppropriate", false);
                } else {
                    System.out.println("Imagen aprobada por el filtro de contenido.");
                }
                
                return result;
            } else {
                String errorMsg = "Error al verificar la imagen: " + response.getStatusCode();
                System.err.println(errorMsg);
                result.put("success", false);
                result.put("message", errorMsg);
                return result;
            }
        } catch (Exception e) {
            String errorMsg = "Error al comunicarse con el servicio de moderación: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            result.put("success", false);
            result.put("message", errorMsg);
            return result;
        }
    }
    
    @SuppressWarnings("unchecked")
    private boolean isImageAppropriate(Map<String, Object> response) {
        try {
            
            System.out.println("=== Análisis de la imagen ===");
            System.out.println("Respuesta completa: " + response);
            
            if (response.containsKey("error")) {
                System.err.println("Error en la respuesta de la API: " + response.get("error"));
                return false; 
            }
            
            if (response.isEmpty()) {
                System.err.println("Respuesta vacía de la API de moderación");
                return false; 
            }
            
            
            final double NUDITY_THRESHOLD = 0.05;      
            final double WEAPON_THRESHOLD = 0.01;      
            final double DRUG_THRESHOLD = 0.05;        
            final double GORE_THRESHOLD = 0.05;        
            final double OFFENSIVE_THRESHOLD = 0.05;   
            final double SCAM_THRESHOLD = 0.05;        
            
            System.out.println("Umbrales de detección:");
            System.out.println("- Desnudos: " + NUDITY_THRESHOLD);
            System.out.println("- Armas: " + WEAPON_THRESHOLD);
            System.out.println("- Drogas: " + DRUG_THRESHOLD);
            System.out.println("- Violencia: " + GORE_THRESHOLD);
            System.out.println("- Contenido ofensivo: " + OFFENSIVE_THRESHOLD);
            System.out.println("- Estafas: " + SCAM_THRESHOLD);
            
            
            if (response.containsKey("nudity")) {
                Map<String, Object> nudity = (Map<String, Object>) response.get("nudity");
                double raw = getDoubleSafely(nudity, "raw");
                double partial = getDoubleSafely(nudity, "partial");
                
                if (raw > NUDITY_THRESHOLD || partial > NUDITY_THRESHOLD) {
                    System.out.println("Contenido inapropiado detectado: Desnudez (raw: " + raw + ", partial: " + partial + ")");
                    return false;
                }
            }
            
            
            if (checkContent(response, "nudity", NUDITY_THRESHOLD) ||
                checkContent(response, "nudity_nsfw", NUDITY_THRESHOLD) ||
                checkContent(response, "nudity_sexual_activity", NUDITY_THRESHOLD) ||
                checkContent(response, "nudity_nudity", NUDITY_THRESHOLD)) {
                System.out.println("Contenido inapropiado detectado: Desnudos");
                return false;
            }
            
            
            boolean hasWeapon = false;
            
            
            if (response.containsKey("weapon")) {
                hasWeapon = getDoubleSafely(response, "weapon") > WEAPON_THRESHOLD;
            }
            
            
            if (!hasWeapon && response.containsKey("wad")) {
                try {
                    Map<String, Object> wad = (Map<String, Object>) response.get("wad");
                    if (wad != null) {
                        hasWeapon = getDoubleSafely(wad, "weapon") > WEAPON_THRESHOLD ||
                                  getDoubleSafely(wad, "weapon_gun") > WEAPON_THRESHOLD ||
                                  getDoubleSafely(wad, "weapon_knife") > WEAPON_THRESHOLD;
                    }
                } catch (Exception e) {
                    System.err.println("Error al verificar WAD: " + e.getMessage());
                }
            }
                              
            if (hasWeapon) {
                System.out.println("Contenido inapropiado detectado: Armas (Confianza: " + 
                    Math.max(
                        getDoubleSafely(response, "weapon"),
                        getDoubleSafely(response, "weapons")
                    ) * 100 + "%)");
                return false;
            }
            
            
            boolean hasDrugs = false;
            
            
            if (response.containsKey("drug")) {
                hasDrugs = getDoubleSafely(response, "drug") > DRUG_THRESHOLD;
            }
            
            if (!hasDrugs && response.containsKey("wad")) {
                try {
                    Map<String, Object> wad = (Map<String, Object>) response.get("wad");
                    if (wad != null) {
                        hasDrugs = getDoubleSafely(wad, "drug") > DRUG_THRESHOLD ||
                                 getDoubleSafely(wad, "drug_needle") > DRUG_THRESHOLD ||
                                 getDoubleSafely(wad, "drug_pill") > DRUG_THRESHOLD ||
                                 getDoubleSafely(wad, "recreational_drug") > DRUG_THRESHOLD;
                    }
                } catch (Exception e) {
                    System.err.println("Error al verificar drogas en WAD: " + e.getMessage());
                }
            }
                             
            if (hasDrugs) {
                System.out.println("Contenido inapropiado detectado: Drogas");
                return false;
            }
            
            
            boolean hasViolence = false;
            
            
            if (response.containsKey("gore")) {
                hasViolence = getDoubleSafely(response, "gore") > GORE_THRESHOLD;
            }
            if (!hasViolence && response.containsKey("violence")) {
                hasViolence = getDoubleSafely(response, "violence") > GORE_THRESHOLD;
            }
            
            
            if (!hasViolence && response.containsKey("wad")) {
                try {
                    Map<String, Object> wad = (Map<String, Object>) response.get("wad");
                    if (wad != null) {
                        hasViolence = getDoubleSafely(wad, "violence") > GORE_THRESHOLD ||
                                    getDoubleSafely(wad, "violence_graphic") > GORE_THRESHOLD ||
                                    getDoubleSafely(wad, "gore_violence") > GORE_THRESHOLD;
                    }
                } catch (Exception e) {
                    System.err.println("Error al verificar violencia en WAD: " + e.getMessage());
                }
            }
                                
            if (hasViolence) {
                System.out.println("Contenido inapropiado detectado: Violencia/Gore");
                return false;
            }
            
            if (response.containsKey("faces")) {
                try {
                    Object facesObj = response.get("faces");
                    if (facesObj instanceof List) {
                        List<?> faces = (List<?>) facesObj;
                        if (!faces.isEmpty()) {
                            for (Object faceObj : faces) {
                                if (faceObj instanceof Map) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> face = (Map<String, Object>) faceObj;
                                    
                                    double faceSize = getDoubleSafely(face, "size");
                                    if (faceSize > 0.3) {
                                        System.out.println("Contenido inapropiado detectado: Cara demasiado grande en la imagen (" + (faceSize*100) + "%)");
                                        return false;
                                    }
                                    
                                    if (face.containsKey("attributes")) {
                                        Map<String, Object> attributes = (Map<String, Object>) face.get("attributes");
                                        
                                        if (getDoubleSafely(attributes, "close") > 0.8) {
                                            System.out.println("Contenido inapropiado detectado: Selfie o primer plano");
                                            return false;
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error al verificar caras: " + e.getMessage());
                    return true;
                }
            }
            
            if (response.containsKey("text") && response.get("text") != null) {
                System.out.println("Advertencia: Se detectó texto en la imagen");
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Error al verificar la imagen: " + e.getMessage());
            e.printStackTrace();
            return false; 
        }
    }
    
    @SuppressWarnings("unchecked")
    private String getRejectionReasons(Map<String, Object> response) {
        List<String> reasons = new ArrayList<>();
        
        
        final double WEAPON_THRESHOLD = 0.5;       
        final double SEXUAL_THRESHOLD = 0.05;      
        final double DRUG_THRESHOLD = 0.5;         
        final double VIOLENCE_THRESHOLD = 0.5;     
        final double ANIME_THRESHOLD = 0.05;       
        
        
        if (checkContent(response, "weapon", WEAPON_THRESHOLD) || 
            checkContent(response, "weapon_firearm", WEAPON_THRESHOLD) || 
            checkContent(response, "weapon_knife", WEAPON_THRESHOLD) ||
            checkContent(response, "weapons", WEAPON_THRESHOLD) ||
            checkContent(response, "knife", WEAPON_THRESHOLD) ||
            checkContent(response, "gun", WEAPON_THRESHOLD)) {
            reasons.add("armas u objetos peligrosos");
        }
        
        
        boolean sexualContentDetected = 
            checkContent(response, "nudity", SEXUAL_THRESHOLD) || 
            checkContent(response, "nudity_nsfw", SEXUAL_THRESHOLD) ||
            checkContent(response, "nudity_sexual_activity", SEXUAL_THRESHOLD) ||
            checkContent(response, "nudity_nudity", SEXUAL_THRESHOLD) ||
            checkContent(response, "sexual_activity", SEXUAL_THRESHOLD) ||
            checkContent(response, "sexual_display", SEXUAL_THRESHOLD) ||
            checkContent(response, "revealing_clothes", SEXUAL_THRESHOLD) ||
            checkContent(response, "bikini", SEXUAL_THRESHOLD) ||
            checkContent(response, "underwear", SEXUAL_THRESHOLD) ||
            checkContent(response, "male_underwear", SEXUAL_THRESHOLD) ||
            checkContent(response, "female_underwear", SEXUAL_THRESHOLD) ||
            checkContent(response, "swimwear", SEXUAL_THRESHOLD) ||
            checkContent(response, "lingerie", SEXUAL_THRESHOLD) ||
            checkContent(response, "cleavage", SEXUAL_THRESHOLD) ||
            checkContent(response, "anime", ANIME_THRESHOLD) ||
            checkContent(response, "cartoon", ANIME_THRESHOLD) ||
            checkContent(response, "drawing", ANIME_THRESHOLD) ||
            checkContent(response, "illustration", ANIME_THRESHOLD);
            
        
        if (!sexualContentDetected && response.containsKey("wad")) {
            try {
                Map<String, Object> wad = (Map<String, Object>) response.get("wad");
                if (wad != null) {
                    double sexualScore = getDoubleSafely(wad, "sexual_activity", 0) + 
                                      getDoubleSafely(wad, "sexual_display", 0) + 
                                      getDoubleSafely(wad, "suggestive", 0);
                    sexualContentDetected = sexualScore >= SEXUAL_THRESHOLD;
                }
            } catch (Exception e) {
                System.err.println("Error al verificar contenido sexual en WAD: " + e.getMessage());
            }
        }
        
        if (sexualContentDetected) {
            reasons.add("contenido sexual o inapropiado");
        }
        
        
        if (checkContent(response, "drug", DRUG_THRESHOLD) || 
            checkContent(response, "drugs", DRUG_THRESHOLD) ||
            checkContent(response, "recreational_drug", DRUG_THRESHOLD) ||
            checkContent(response, "cigarette", DRUG_THRESHOLD) ||
            checkContent(response, "cigar", DRUG_THRESHOLD) ||
            checkContent(response, "tobacco", DRUG_THRESHOLD) ||
            checkContent(response, "alcohol", DRUG_THRESHOLD) ||
            checkContent(response, "pills", DRUG_THRESHOLD)) {
            reasons.add("drogas, alcohol o sustancias ilegales");
        }
        
        
        if (checkContent(response, "violence", VIOLENCE_THRESHOLD) || 
            checkContent(response, "gore", VIOLENCE_THRESHOLD) ||
            checkContent(response, "blood", VIOLENCE_THRESHOLD) ||
            checkContent(response, "fight", VIOLENCE_THRESHOLD) ||
            checkContent(response, "weapon", VIOLENCE_THRESHOLD) ||
            checkContent(response, "gun", VIOLENCE_THRESHOLD) ||
            checkContent(response, "knife", VIOLENCE_THRESHOLD)) {
            reasons.add("contenido violento o explícito");
        }
        
        
        if (reasons.isEmpty() && response.containsKey("wad")) {
            try {
                Map<String, Object> wad = (Map<String, Object>) response.get("wad");
                if (wad != null) {
                    if (getDoubleSafely(wad, "weapon") > 0.01 || 
                        getDoubleSafely(wad, "weapon_gun") > 0.01 || 
                        getDoubleSafely(wad, "weapon_knife") > 0.01) {
                        reasons.add("armas u objetos peligrosos");
                    }
                }
            } catch (Exception e) {
                System.err.println("Error al verificar WAD: " + e.getMessage());
            }
        }
        
        if (reasons.isEmpty()) {
            return "contenido no permitido";
        }
        
        return String.join(", ", reasons);
    }
    
    
    private double getDoubleSafely(Map<String, Object> map, String key) {
        return getDoubleSafely(map, key, 0.0);
    }
    
    
    private double getDoubleSafely(Map<String, Object> map, String key, double defaultValue) {
        try {
            if (map == null || !map.containsKey(key)) {
                return defaultValue;
            }
            Object value = map.get(key);
            if (value == null) {
                return defaultValue;
            }
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            System.err.println("Error al obtener valor numérico para " + key + ": " + e.getMessage());
            return defaultValue;
        }
    }
    
    
    @SuppressWarnings("unchecked")
    private boolean checkContent(Map<String, Object> response, String key, double threshold) {
        if (!response.containsKey(key)) {
            return false;
        }
        
        try {
            double prob = 0.0;
            Object value = response.get(key);
            
            
            if (value instanceof Map) {
                Map<String, Object> data = (Map<String, Object>) value;
                prob = getDoubleSafely(data, "prob");
            } else if (value instanceof Number) {
                prob = ((Number) value).doubleValue();
            } else if (value instanceof String) {
                try {
                    prob = Double.parseDouble((String) value);
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            
            if (prob > threshold) {
                System.out.println("Contenido inapropiado detectado: " + key + " (prob: " + prob + ")");
                return true;
            }
            
            return false;
        } catch (Exception e) {
            System.err.println("Error al verificar " + key + ": " + e.getMessage());
            return false;
        }
    }
}
