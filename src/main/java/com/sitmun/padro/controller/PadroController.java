package com.sitmun.padro.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/padron")
public class PadroController {

    @GetMapping("/{municipality}/{nucleus}/{INECode}")
    public String hello(
            @PathVariable String municipality,
            @PathVariable String nucleus,
            @PathVariable String INECode,
            @RequestParam String portal,
            @RequestParam String letterFrom,
            @RequestParam String floor,
            @RequestParam String door
    ) {
        return "Padron microservice is running";
    }

}
