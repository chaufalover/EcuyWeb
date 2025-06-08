package com.project.ecuy.controller;

import com.project.ecuy.entities.Activity;
import com.project.ecuy.entities.VideoWord;
import com.project.ecuy.services.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/video-words")
public class VideoWordController {

    private final ActivityService activityService;

    @Autowired
    public VideoWordController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping("/activity/{activityId}")
    public ResponseEntity<List<VideoWord>> getVideoWordsByActivity(@PathVariable Long activityId) {
        Activity activity = activityService.getActivityWithVideoWords(activityId);
        return ResponseEntity.ok(activity.getPalabrasVideo());
    }

    @GetMapping("/activity/{activityId}/ordered")
    public ResponseEntity<List<VideoWord>> getOrderedVideoWords(@PathVariable Long activityId) {
        Activity activity = activityService.getActivityWithOrderedVideoWords(activityId);
        return ResponseEntity.ok(activity.getPalabrasVideo());
    }

    @PostMapping("/activity/{activityId}/words")
    public ResponseEntity<Void> addVideoWordsToActivity(
            @PathVariable Long activityId,
            @RequestBody List<VideoWord> words) {
        activityService.addVideoWordsToActivity(activityId, words);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/activity/{activityId}/check-order")
    public ResponseEntity<Boolean> checkWordOrder(
            @PathVariable Long activityId,
            @RequestBody List<Long> wordIds) {
        Activity activity = activityService.getActivityWithOrderedVideoWords(activityId);
        List<VideoWord> correctOrder = activity.getPalabrasVideo();
        
        if (wordIds.size() != correctOrder.size()) {
            return ResponseEntity.ok(false);
        }
        
        for (int i = 0; i < wordIds.size(); i++) {
            if (!wordIds.get(i).equals(correctOrder.get(i).getId())) {
                return ResponseEntity.ok(false);
            }
        }
        
        return ResponseEntity.ok(true);
    }
}
