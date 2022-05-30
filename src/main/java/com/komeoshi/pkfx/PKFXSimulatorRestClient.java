package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Instrument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class PKFXSimulatorRestClient {
    private static final Logger log = LoggerFactory.getLogger(PKFXSimulatorRestClient.class);

    public Instrument run(RestTemplate restTemplate) {

        String url = "https://api-fxpractice.oanda.com/v3/instruments/USD_JPY/candles?";
        url += "count=5000";
        url += "&granularity=M1";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer 8a93d17e57500d5a9b13c4a27f74b179-4aee32e93758790ec1d3fa0eb24f1aed");

        ResponseEntity<Instrument> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<Instrument>() {
                });
        Instrument instrument = response.getBody();

        log.info("response size:" + instrument.getCandles().size());

        return instrument;
    }

}
