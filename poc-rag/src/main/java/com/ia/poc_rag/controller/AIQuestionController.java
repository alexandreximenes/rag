package com.ia.poc_rag.controller;

import com.ia.poc_rag.service.AIQuestionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.LinkedHashSet;

@RestController
@RequestMapping("api/v1")
public class AIQuestionController {

    private final AIQuestionService springAIQuestionService;

    public AIQuestionController(AIQuestionService springAIQuestionService) {
        this.springAIQuestionService = springAIQuestionService;
    }

    @GetMapping("/ai/chat")
    public Flux<String> chat(@RequestParam String message,
                             @RequestHeader(value = "username", defaultValue = "anonymous")
                             String username){
        return springAIQuestionService.chat(message, username);
    }

    @GetMapping("/ai/web-search/chat")
    public Flux<String> webSearchchat(@RequestParam String message,
                             @RequestHeader(value = "username", defaultValue = "anonymous")
                             String username){
        return springAIQuestionService.webSearchChat(message, username);
    }

    @PostMapping(
            path = "/ai/upload/files",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadPdf(@RequestParam("file") LinkedHashSet<MultipartFile> files) {
        springAIQuestionService.uploadFile(files);
        return ResponseEntity.ok().body("File uploaded successfully.");
    }
}
