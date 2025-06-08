package com.project.ecuy.services;

import com.project.ecuy.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public abstract class BaseService<T, ID extends Serializable, R extends JpaRepository<T, ID>> {
    
    protected final R repository;
    
    protected BaseService(R repository) {
        this.repository = repository;
    }
    
    public List<T> findAll() {
        return repository.findAll();
    }
    
    public Optional<T> findById(ID id) {
        return repository.findById(id);
    }
    
    public T getById(ID id) {
        return findById(id).orElseThrow(() -> 
            new ResourceNotFoundException("Resource not found with id: " + id)
        );
    }
    
    public T save(T entity) {
        return repository.save(entity);
    }
    
    public void deleteById(ID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Resource not found with id: " + id);
        }
        repository.deleteById(id);
    }
    
    public boolean existsById(ID id) {
        return repository.existsById(id);
    }
}
