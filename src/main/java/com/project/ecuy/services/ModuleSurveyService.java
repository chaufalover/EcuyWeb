package com.project.ecuy.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.project.ecuy.entities.ModuleSurvey;
import com.project.ecuy.entities.User;
import com.project.ecuy.repository.ModuleSurveyRepository;
import com.project.ecuy.entities.Module;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ModuleSurveyService {
    private final ModuleSurveyRepository surveyRepository;

    public ModuleSurvey guardarEncuesta(ModuleSurvey encuesta) {
        return surveyRepository.save(encuesta);
    }

    public boolean encuestaYaRegistrada(User user, Module module) {
        return surveyRepository.existsByUsuarioAndModulo(user, module);
    }

    public List<ModuleSurvey> obtenerEncuestasPorModulo(Module module) {
        return surveyRepository.findByModulo(module);
    }

    public List<Object[]> obtenerResumenPorCalificacion(Long moduleId) {
        return surveyRepository.countByCalificacionGrouped(moduleId);
    }

    public List<ModuleSurvey> obtenerEncuestas() {
        return surveyRepository.findAllByOrderByFechaCreacionDesc();
    }

}
