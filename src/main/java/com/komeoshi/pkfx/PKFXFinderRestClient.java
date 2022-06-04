package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Account;
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

    public Account getAccount(RestTemplate restTemplate) {
        String url = "https://" + PKFXConst.API_DOMAIN + "/v3/accounts/" +
                PKFXConst.ACCOUNT_ID + "/summary?";

        HttpHeaders headers = getHttpHeaders();
        ResponseEntity<Account> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<Account>() {
                });

        return response.getBody();
    }

    /**
     * 最新のローソク情報を取得する.
     *
     * @param restTemplate RestTemplate
     * @return Instrument
     */
    public Instrument getInstrument(RestTemplate restTemplate) {

        String url = "https://" + PKFXConst.API_DOMAIN + "/v3/instruments/USD_JPY/candles?";
        url += "count=100";
        url += "&granularity=M1";

        HttpHeaders headers = getHttpHeaders();

        ResponseEntity<Instrument> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<Instrument>() {
                });

        return response.getBody();
    }

    public void buy(RestTemplate restTemplate) {
        String url = "https://" + PKFXConst.API_DOMAIN + "/v3/accounts/" + PKFXConst.ACCOUNT_ID + "/orders";

        HttpHeaders headers = getHttpHeaders();
        String body = "{\"order\":{\"units\":\"" + PKFXConst.DEFAULT_UNIT +
                "\",\"instrument\":\"USD_JPY\",\"timeInForce\":\"FOK\",\"type\":\"MARKET\",\"positionFill\":\"DEFAULT\"}}";

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        // リクエストの送信
        restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
    }

    public void sell(RestTemplate restTemplate) {
        HttpHeaders headers = getHttpHeaders();

        // 保有しているポジションを取得
        Trades trades = getTrades(restTemplate, headers);

        for (int i = 0; i < trades.getTrades().size(); i++) {
            // 保有しているポジションを決済する
            Trade trade = trades.getTrades().get(i);

            String sellUrl = "https://" + PKFXConst.API_DOMAIN + "/v3/accounts/" + PKFXConst.ACCOUNT_ID + "/trades/";
            sellUrl += "" + trade.getId();
            sellUrl += "/close";

            restTemplate.exchange(
                    sellUrl,
                    HttpMethod.PUT,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<String>() {
                    });

        }
    }

    private Trades getTrades(RestTemplate restTemplate, HttpHeaders headers) {
        String url = "https://" + PKFXConst.API_DOMAIN + "/v3/accounts/" + PKFXConst.ACCOUNT_ID + "/trades?instrument=USD_JPY";
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
        c.sell(new RestTemplate());
    }
}
