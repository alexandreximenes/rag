package com.ia.poc_rag.controller;

import com.ia.poc_rag.service.RhService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("api/v1")
public class RhController {

    private final RhService rhService;

    public RhController(RhService rhService) {
        this.rhService = rhService;
    }

    @GetMapping("/rh/chat")
    public Flux<String> chat(@RequestParam String message,
                             @RequestHeader(value = "username", defaultValue = "anonymous")
                             String username){
        return rhService.chat(message, username);
    }
}
