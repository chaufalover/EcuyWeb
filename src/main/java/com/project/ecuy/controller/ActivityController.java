package com.project.ecuy.controller;

import com.project.ecuy.entities.Activity;
import com.project.ecuy.services.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/actividad")
public class ActivityController {

    private final ActivityService activityService;

    @Autowired
    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<Activity>> getActivitiesByModule(@PathVariable Long moduleId) {
        return ResponseEntity.ok(activityService.findByModuleId(moduleId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Activity> getActivityById(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.getById(id));
    }

    @GetMapping("/{id}/contents")
    public ResponseEntity<Activity> getActivityWithContents(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.getActivityWithContents(id));
    }

    @GetMapping("/{id}/quiz-options")
    public ResponseEntity<Activity> getActivityWithQuizOptions(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.getActivityWithQuizOptions(id));
    }

    @GetMapping("/{id}/matching-pairs")
    public ResponseEntity<Activity> getActivityWithMatchingPairs(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.getActivityWithMatchingPairs(id));
    }

    @PostMapping("/module/{moduleId}")
    public ResponseEntity<Activity> createActivity(
            @PathVariable Long moduleId,
            @RequestBody Activity activity) {
        return ResponseEntity.ok(activityService.createActivityForModule(activity, moduleId));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Activity> updateActivity(
            @PathVariable Long id,
            @RequestBody Activity activityDetails) {
        return ResponseEntity.ok(activityService.updateActivity(id, activityDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActivity(@PathVariable Long id) {
        activityService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
