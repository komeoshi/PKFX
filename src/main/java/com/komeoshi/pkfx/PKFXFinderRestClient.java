package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Instrument;
import com.komeoshi.pkfx.dto.Trade;
import com.komeoshi.pkfx.dto.Trades;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class PKFXFinderRestClient {
    private static final Logger log = LoggerFactory.getLogger(PKFXFinderRestClient.class);

    public Instrument getInstrument(RestTemplate restTemplate) {

        String url = "https://api-fxpractice.oanda.com/v3/instruments/USD_JPY/candles?";
        url += "count=1";
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

        return instrument;
    }

    public void buy(RestTemplate restTemplate) {
        String url = "https://api-fxpractice.oanda.com/v3/accounts/101-009-22492304-001/orders";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer 8a93d17e57500d5a9b13c4a27f74b179-4aee32e93758790ec1d3fa0eb24f1aed");

        String body = "{\"order\":{\"units\":\"4500\",\"instrument\":\"USD_JPY\",\"timeInForce\":\"FOK\",\"type\":\"MARKET\",\"positionFill\":\"DEFAULT\"}}";

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        //リクエストの送信
        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
        String res = response.getBody();
        // log.info(res);

    }

    public void sell(RestTemplate restTemplate) {
        String url = "https://api-fxpractice.oanda.com/v3/accounts/101-009-22492304-001/trades?instrument=USD_JPY";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer 8a93d17e57500d5a9b13c4a27f74b179-4aee32e93758790ec1d3fa0eb24f1aed");

        ResponseEntity<Trades> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<Trades>() {
                });
        Trades trades = response.getBody();

        for (int i = 0; i < trades.getTrades().size(); i++) {
            Trade trade = trades.getTrades().get(i);

            String sellUrl = "https://api-fxpractice.oanda.com/v3/accounts/101-009-22492304-001/trades/";
            sellUrl += "" + trade.getId();
            sellUrl += "/close";

            ResponseEntity<String> sellResponse = restTemplate.exchange(
                    sellUrl,
                    HttpMethod.PUT,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<String>() {
                    });

        }
    }
}
