package com.komeoshi.pkfx.restclient;

import com.komeoshi.pkfx.PKFXConst;
import com.komeoshi.pkfx.dto.Candle;
import com.komeoshi.pkfx.dto.Instrument;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PKFXSimulatorRestClient {
    private static final Logger log = LoggerFactory.getLogger(PKFXSimulatorRestClient.class);

    public static void main(String[] args) {
        PKFXSimulatorRestClient client = new PKFXSimulatorRestClient();
        log.info("" + client.getMins());

    }

    private List<String> getMins() {
        LocalDateTime from = LocalDateTime.of(2012, 1, 1, 0, 0, 0, 0);
        LocalDateTime to = LocalDateTime.of(2012, 1, 1, 1, 0, 0, 0);

        List<String> days = new ArrayList<>();
        while (from.isBefore(LocalDateTime.of(2022, 6, 11, 0, 0, 0, 0))) {
            String dateFrom = from.format(DateTimeFormatter.ISO_DATE_TIME);
            String dateTo = to.format(DateTimeFormatter.ISO_DATE_TIME);

            String day = "from=" + dateFrom + ".000000000Z&to=" + dateTo + ".000000000Z";
            days.add(day);
            from = from.plusHours(1);
            to = to.plusHours(1);
        }

        return days;
    }

    private List<String> getDays() {
        LocalDateTime from = LocalDateTime.of(2012, 1, 1, 0, 0, 0, 0);
        LocalDateTime to = LocalDateTime.of(2012, 1, 2, 0, 0, 0, 0);

        List<String> days = new ArrayList<>();
        while (from.isBefore(LocalDateTime.of(2022, 6, 11, 0, 0, 0, 0))) {
            String dateFrom = from.format(DateTimeFormatter.ISO_DATE_TIME);
            String dateTo = to.format(DateTimeFormatter.ISO_DATE_TIME);

            String day = "from=" + dateFrom + ".000000000Z&to=" + dateTo + ".000000000Z";
            days.add(day);
            from = from.plusDays(1);
            to = to.plusDays(1);
        }

        return days;
    }

    public List<Candle> runMins(RestTemplate restTemplate) {
        List<Candle> candles = new ArrayList<>();
        List<String> mins = getMins();

        for (String min : mins) {
            log.info(min);
            HttpComponentsClientHttpRequestFactory httpComponents =
                    new HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create().build());
            candles.addAll(run(new RestTemplate(httpComponents), min, "S5").getCandles());
        }

        log.info("size:" + candles.size());
        return candles;
    }

    public List<Candle> runWithManyCandles(RestTemplate restTemplate) {

        List<Candle> candles = new ArrayList<>();
        List<String> days = getDays();

        for (String day : days) {
            log.info(day);
//            HttpComponentsClientHttpRequestFactory httpComponents =
//                    new HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create().build());
            candles.addAll(run(new RestTemplate(), day, PKFXConst.GRANULARITY).getCandles());
        }

        log.info("size:" + candles.size());
        return candles;
    }

    private Instrument run(RestTemplate restTemplate, String day, String granularity) {
        String url = "https://" + PKFXConst.getApiDomain() + "/v3/instruments/" + PKFXConst.CURRENCY + "/candles?";
        url += day;
        url += "&granularity=" + granularity;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + PKFXConst.getApiAccessToken());
        // headers.add("Accept-Encoding", "gzip, deflate");

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

    public Instrument run(RestTemplate restTemplate) {

        String url = "https://" + PKFXConst.getApiDomain() + "/v3/instruments/" + PKFXConst.CURRENCY + "/candles?";
        url += "count=5000";
        url += "&granularity=" + PKFXConst.GRANULARITY;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + PKFXConst.getApiAccessToken());
        //  headers.add("Accept-Encoding","gzip, deflate");

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
