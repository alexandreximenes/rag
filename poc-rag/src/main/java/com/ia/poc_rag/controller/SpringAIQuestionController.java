package com.ia.poc_rag.controller;

import com.ia.poc_rag.service.SpringIAQuestionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.LinkedHashSet;

@RestController
@RequestMapping("api/v1")
public class SpringAIQuestionController {

    private final SpringIAQuestionService springAIQuestionService;

    public SpringAIQuestionController(SpringIAQuestionService springAIQuestionService) {
        this.springAIQuestionService = springAIQuestionService;
    }

    @GetMapping("/spring-ai/chat")
    public Flux<String> chat(@RequestParam String message,
                             @RequestHeader(value = "username", defaultValue = "anonymous")
                             String username){
        return springAIQuestionService.chat(message, username);
    }

    @PostMapping(
            path = "/spring-ai/upload/files",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadPdf(@RequestParam("file") LinkedHashSet<MultipartFile> files) {
        springAIQuestionService.uploadFile(files);
        return ResponseEntity.ok().body("File uploaded successfully.");
    }
}
