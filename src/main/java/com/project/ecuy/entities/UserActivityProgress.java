package com.project.ecuy.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_activity_progress")
public class UserActivityProgress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;
    
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "puntos_obtenidos")
    private Integer puntosObtenidos = 0;
    
    @Column(name = "puntos_totales")
    private Integer puntosTotales = 5; 
    
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public Integer getPuntosObtenidos() {
        return puntosObtenidos;
    }
    
    public void setPuntosObtenidos(Integer puntosObtenidos) {
        this.puntosObtenidos = puntosObtenidos;
    }
    
    public Integer getPuntosTotales() {
        return puntosTotales;
    }
    
    public void setPuntosTotales(Integer puntosTotales) {
        this.puntosTotales = puntosTotales;
    }
}
