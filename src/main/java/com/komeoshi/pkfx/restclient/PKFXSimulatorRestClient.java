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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PKFXSimulatorRestClient {
    private static final Logger log = LoggerFactory.getLogger(PKFXSimulatorRestClient.class);

    public static void main(String[] args) {
        PKFXSimulatorRestClient client = new PKFXSimulatorRestClient();
        List<Candle> candles = client.runWithManyCandles(new RestTemplate());

        log.info("" + candles.size());
    }

    private List<String> getMins(LocalDate _from, LocalDate _to) {
        LocalDateTime from = LocalDateTime.of(_from.getYear(), _from.getMonthValue(), _from.getDayOfMonth(), 0, 0, 0, 0);
        LocalDateTime fromTmp = from.plusHours(1);

        List<String> mins = new ArrayList<>();
        while (from.isBefore(LocalDateTime.of(_to.getYear(), _to.getMonthValue(), _to.getDayOfMonth(), 0, 0, 0, 0))) {
            String dateFrom = from.format(DateTimeFormatter.ISO_DATE_TIME);
            String dateTo = fromTmp.format(DateTimeFormatter.ISO_DATE_TIME);

            String day = "from=" + dateFrom + ".000000000Z&to=" + dateTo + ".000000000Z";
            mins.add(day);
            from = from.plusHours(1);
            fromTmp = fromTmp.plusHours(1);
        }

        return mins;
    }

    private List<String> getDays(LocalDate _from, LocalDate _to) {
        LocalDateTime from = LocalDateTime.of(_from.getYear(), _from.getMonthValue(), _from.getDayOfMonth(), 0, 0, 0, 0);
        LocalDateTime fromTmp = from.plusDays(1);

        List<String> days = new ArrayList<>();
        while (from.isBefore(LocalDateTime.of(_to.getYear(), _to.getMonthValue(), _to.getDayOfMonth(), 0, 0, 0, 0))) {
            String dateFrom = from.format(DateTimeFormatter.ISO_DATE_TIME);
            String dateTo = fromTmp.format(DateTimeFormatter.ISO_DATE_TIME);

            String day = "from=" + dateFrom + ".000000000Z&to=" + dateTo + ".000000000Z";
            days.add(day);
            from = from.plusDays(1);
            fromTmp = fromTmp.plusDays(1);
        }

        return days;
    }

    public List<Candle> runMins(RestTemplate restTemplate) {
        return runMins(restTemplate,
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2022, 7, 5));
    }

    public List<Candle> runWithManyCandles(RestTemplate restTemplate) {
        return runWithManyCandles(restTemplate,
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2022, 7, 5));
    }

    public List<Candle> runMins(RestTemplate restTemplate, LocalDate _from, LocalDate _to) {
        List<Candle> candles = new ArrayList<>();
        List<String> mins = getMins(_from, _to);

        for (String min : mins) {
            log.info(min);
            candles.addAll(run(new RestTemplate(), min, "S5").getCandles());
        }

        log.info("size:" + candles.size());
        return candles;
    }

    public List<Candle> runWithManyCandles(RestTemplate restTemplate, LocalDate _from, LocalDate _to) {

        List<Candle> candles = new ArrayList<>();
        List<String> days = getDays(_from, _to);

        for (String day : days) {
            log.info(day);
            candles.addAll(run(new RestTemplate(), day, PKFXConst.GRANULARITY).getCandles());
        }

        log.info("size:" + candles.size());
        return candles;
    }

    private Instrument run(RestTemplate restTemplate, String day, String granularity) {
        String url = "https://" + PKFXConst.getApiDomain() + "/v3/instruments/" + PKFXConst.CURRENCY + "/candles?";
        url += day;
        url += "&granularity=" + granularity;
        url += "&price=BA";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + PKFXConst.getApiAccessToken());

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
