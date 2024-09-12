package ru.mpei.llmconverter.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.mpei.llmconverter.model.RequestObject;
import ru.mpei.llmconverter.service.ApiService;

@RestController
@CrossOrigin
@Slf4j
public class ApiController {
    @Autowired
    private ApiService service;

    @PostMapping("/api/convert")
    public String convertLLMResponse(@RequestBody RequestObject body) {
        log.warn(body.getRequest());
        this.service.extract_data(body.getRequest());
        return "hi";
    }
}
