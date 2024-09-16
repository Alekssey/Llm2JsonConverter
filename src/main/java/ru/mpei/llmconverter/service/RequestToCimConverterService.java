package ru.mpei.llmconverter.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.mpei.llmconverter.model.Scheme;

@Service
public class RequestToCimConverterService {
    @Value("${cim.converter.ip}")
    private String ip;
    @Value("${cim.converter.port}")
    private String port;
    private final RestTemplate rt = new RestTemplate();
    private String url;

    @PostConstruct
    private void buildUrl() {
        this.url = "http://" + this.ip + ":" + this.port + "/converter/json-to-cim";
    }
    public String sendSchemeForConvertingToCim(Scheme scheme){
        System.err.println(this.url);
        ResponseEntity<String> ent = rt.postForEntity(url, scheme, String.class);
//        System.err.println(ent.getBody());
        return ent.getBody();
    }

}
