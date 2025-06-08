package com.project.ecuy.controller;

import com.project.ecuy.entities.UserProgress;
import com.project.ecuy.services.UserProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
public class UserProgressController {

    private final UserProgressService userProgressService;

    @Autowired
    public UserProgressController(UserProgressService userProgressService) {
        this.userProgressService = userProgressService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserProgress>> getUserProgress(@PathVariable Integer userId) {
        return ResponseEntity.ok(userProgressService.findByUserId(userId));
    }

    @GetMapping("/user/{userId}/module/{moduleId}")
    public ResponseEntity<UserProgress> getUserModuleProgress(
            @PathVariable Integer userId,
            @PathVariable Long moduleId) {
        return ResponseEntity.ok(userProgressService.findByUserAndModule(userId, moduleId));
    }

    @PutMapping("/user/{userId}/module/{moduleId}")
    public ResponseEntity<UserProgress> updateUserProgress(
            @PathVariable Integer userId,
            @PathVariable Long moduleId,
            @RequestParam int score) {
        return ResponseEntity.ok(userProgressService.updateScore(userId, moduleId, score));
    }

    @GetMapping("/user/{userId}/total-score")
    public ResponseEntity<Integer> getUserTotalScore(@PathVariable Integer userId) {
        return ResponseEntity.ok(userProgressService.getTotalScoreByUser(userId));
    }
}
