package ru.mpei.llmconverter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.mpei.llmconverter.service.ApiService;

@RestController
@CrossOrigin
public class ApiController {
    @Autowired
    private ApiService service;

    @PostMapping("/api/convert")
    public String convertLLMResponse(@RequestBody String body) {
        this.service.extract_data(body);
        return "hi";
    }
}
