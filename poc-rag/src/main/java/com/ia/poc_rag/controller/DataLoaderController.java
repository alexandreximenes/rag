package com.ia.poc_rag.controller;

import com.ia.poc_rag.service.RandomDataLoaderService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("api/v1")
public class DataLoaderController {

    private final RandomDataLoaderService randomDataLoaderService;

    public DataLoaderController(RandomDataLoaderService randomDataLoaderService) {
        this.randomDataLoaderService = randomDataLoaderService;
    }

    @GetMapping("/data-loader/chat")
    public Flux<String> chat(@RequestParam String message,
                             @RequestHeader(value = "username", defaultValue = "anonymous")
                             String username){
        return randomDataLoaderService.chat(message, username);
    }
}
