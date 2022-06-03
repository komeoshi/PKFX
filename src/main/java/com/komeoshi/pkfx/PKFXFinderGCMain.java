package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Candle;
import com.komeoshi.pkfx.dto.Instrument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class PKFXFinderGCMain {

    private static final Logger log = LoggerFactory.getLogger(PKFXFinderGCMain.class);

    public static void main(String[] args) {
        SpringApplication.run(PKFXFinderGCMain.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
        return args -> {

            PKFXFinderRestClient client = new PKFXFinderRestClient();

            Status status = Status.NONE;
            Position lastPosition = Position.NONE;
            Candle openCandle = null;
            boolean initialBuy = false;

            while (true) {
                Instrument instrument = getInstrument(restTemplate, client);
                if (instrument == null) continue;

                setPosition(instrument.getCandles());

                Candle candle = instrument.getCandles().get(instrument.getCandles().size() - 1);

                if (!initialBuy) {
                    if (candle.getPosition() == Position.LONG) {
                        initialBuy = true;
                        log.info("signal (initialBuy)>>");
                    } else {
                        continue;
                    }
                }

                if (candle.getPosition() != lastPosition) {
                    if (candle.getPosition() == Position.LONG) {
                        log.info("signal>> " + candle.getTime() + ", OPEN:" + candle.getMid().getO() + ", HIGH:" + candle.getMid().getH());

                        client.buy(restTemplate);
                        status = Status.HOLDING;
                        lastPosition = candle.getPosition();

                        openCandle = candle;
                    } else if (candle.getPosition() == Position.SHORT && status == Status.HOLDING) {
                        log.info("<<signal (timeout)" + candle.getTime() + ", OPEN:" + candle.getMid().getO() + ", HIGH:" + candle.getMid().getH());

                        client.sell(restTemplate);
                        status = Status.NONE;
                        lastPosition = candle.getPosition();
                    }
                }

                double targetRate = openCandle.getMid().getC() * 1.00015;
                if (status == Status.HOLDING &&
                        targetRate < candle.getMid().getC()) {
                    log.info("<<signal (reached)" + candle.getTime() + ", OPEN:" + candle.getMid().getO() + ", HIGH:" + candle.getMid().getH());

                    client.sell(restTemplate);
                    status = Status.NONE;
                    lastPosition = Position.SHORT;
                }
            }
        };

    }

    private Instrument getInstrument(RestTemplate restTemplate, PKFXFinderRestClient client) {
        Instrument i;
        try {
            i = client.getInstrument(restTemplate);
        } catch (RestClientException e) {
            log.error("", e);
            return null;
        }
        return i;
    }

    private void setPosition(List<Candle> candles) {
        for (int ii = 0; ii < candles.size(); ii++) {
            Candle currentCandle = candles.get(ii);
            currentCandle.setNumber(ii);
            if (ii < 75) {
                continue;
            }

            List<Candle> currentCandles = new ArrayList<>();

            for (int jj = 0; jj < ii; jj++) {
                currentCandles.add(candles.get(jj));
            }
            PKFXFinderAnalyzer finder = new PKFXFinderAnalyzer(currentCandle);
            double shortMa = finder.getMa(currentCandles, 9);
            double longMa = finder.getMa(currentCandles, 26);

            if (shortMa > longMa) {
                currentCandle.setPosition(Position.LONG);
            } else {
                currentCandle.setPosition(Position.SHORT);
            }
            // log.info(currentCandle.getTime() + " " + currentCandle.getPosition());
        }
    }

}
