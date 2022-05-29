package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Instrument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class PKFXRestClient {
    private static final Logger log = LoggerFactory.getLogger(PKFXRestClient.class);

    public Instrument run(RestTemplate restTemplate) {

        String url = "http://api-sandbox.oanda.com/v1/candles?instrument=EUR_USD";
        url += "&count=5000";
        url += "&granularity=M1";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("apikey", "xxxxxxxxxxxxxxxxxxxxxxxxxxx");

        ResponseEntity<List<Instrument>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<Instrument>>() {
                });
        List<Instrument> instruments = response.getBody();

        log.info(instruments.toString());

        return instruments.get(0);
    }

}
