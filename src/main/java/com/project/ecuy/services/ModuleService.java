package com.project.ecuy.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.project.ecuy.entities.Activity;
import com.project.ecuy.entities.Module;
import com.project.ecuy.repository.ModuleRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleRepository repository;

    public int totalModulos() {
        return repository.contador();
    }

    public List<Module> listarModulosActivos() {
        return repository.findAllActiveModulesNative();
    }

    public Optional<Module> buscarPorId(Long id) {
        return repository.findById(id);
    }

  
}
