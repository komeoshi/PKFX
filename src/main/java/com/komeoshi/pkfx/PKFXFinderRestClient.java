package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Set;

public class PKFXFinderRestClient {
    private static final Logger log = LoggerFactory.getLogger(PKFXFinderRestClient.class);

    public Account getAccount(RestTemplate restTemplate) {
        String url = "https://" + PKFXConst.API_DOMAIN + "/v3/accounts/" +
                PKFXConst.ACCOUNT_ID + "/summary?";

        HttpHeaders headers = getHttpHeaders();
        ResponseEntity<AccountSummary> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<AccountSummary>() {
                });

        return response.getBody().getAccount();
    }

    /**
     * 最新のローソク情報を取得する.
     *
     * @param restTemplate RestTemplate
     * @return Instrument
     */
    public Instrument getInstrument(RestTemplate restTemplate) {

        String url = "https://" + PKFXConst.API_DOMAIN + "/v3/instruments/" + PKFXConst.CURRENCY + "/candles?";
        url += "count=100";
        url += "&granularity=" + PKFXConst.GRANULARITY;

        HttpHeaders headers = getHttpHeaders();

        ResponseEntity<Instrument> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<Instrument>() {
                });

        return response.getBody();
    }

    public void buy(double currentPrice, RestTemplate restTemplate) {

        PKFXUnitCalculator unitCalc = new PKFXUnitCalculator();
        int unit = unitCalc.calculate(currentPrice, restTemplate);

        String url = "https://" + PKFXConst.API_DOMAIN + "/v3/accounts/" + PKFXConst.ACCOUNT_ID + "/orders";

        HttpHeaders headers = getHttpHeaders();
        String body = "{\"order\":{\"units\":\"" + unit +
                "\",\"instrument\":\"" + PKFXConst.CURRENCY + "\",\"timeInForce\":\"FOK\",\"type\":\"MARKET\",\"positionFill\":\"DEFAULT\"}}";

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        // リクエストの送信
        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
        log.info(response.getBody());
    }

    public void sell(double currentPrice, RestTemplate restTemplate) {

        PKFXUnitCalculator unitCalc = new PKFXUnitCalculator();
        int unit = unitCalc.calculate(currentPrice, restTemplate);
        unit = -unit;

        String url = "https://" + PKFXConst.API_DOMAIN + "/v3/accounts/" + PKFXConst.ACCOUNT_ID + "/orders";

        HttpHeaders headers = getHttpHeaders();
        String body = "{\"order\":{\"units\":\"" + unit +
                "\",\"instrument\":\"" + PKFXConst.CURRENCY + "\",\"timeInForce\":\"FOK\",\"type\":\"MARKET\",\"positionFill\":\"DEFAULT\"}}";

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        // リクエストの送信
        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
        log.info(response.getBody());
    }

    public void complete(RestTemplate restTemplate) {
        HttpHeaders headers = getHttpHeaders();

        // 保有しているポジションを取得
        Trades trades = getTrades(restTemplate, headers);

        for (int i = 0; i < trades.getTrades().size(); i++) {
            // 保有しているポジションを決済する
            Trade trade = trades.getTrades().get(i);

            String sellUrl = "https://" + PKFXConst.API_DOMAIN + "/v3/accounts/" + PKFXConst.ACCOUNT_ID + "/trades/";
            sellUrl += "" + trade.getId();
            sellUrl += "/close";

            ResponseEntity<String> response = restTemplate.exchange(
                    sellUrl,
                    HttpMethod.PUT,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<String>() {
                    });

            log.info(response.getBody());
            sleep();

        }
    }

    private void sleep() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {

        }
    }

    private Trades getTrades(RestTemplate restTemplate, HttpHeaders headers) {
        String url = "https://" + PKFXConst.API_DOMAIN + "/v3/accounts/" + PKFXConst.ACCOUNT_ID + "/trades?instrument=" + PKFXConst.CURRENCY;
        ResponseEntity<Trades> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<Trades>() {
                });
        return response.getBody();
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + PKFXConst.API_ACCESS_TOKEN);
        return headers;
    }

    public static void main(String[] args){
        PKFXFinderRestClient c = new PKFXFinderRestClient();
        c.buy(130.80, new RestTemplate());
    }
}
